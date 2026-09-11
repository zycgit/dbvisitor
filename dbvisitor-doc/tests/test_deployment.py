"""Offline deployment contracts: no real credentials, OSS uploads or CDN calls."""

import contextlib
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import types
import unittest
from unittest.mock import Mock, patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import deploy_config
import deploy_to_oss
import refresh_website_cdn
from deploy_site import SITE_NAME


def config_fixture():
    return {"accessKeyId": "test-id", "accessKeySecret": "test-secret",
            "endpoint": "https://oss.example.test", "bucketName": "test-docs",
            "cdnUrl": "https://docs.example.test/hasor", "prefix": "", "siteName": SITE_NAME}




class OssDeploymentTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.document = Path(self.temporary.name)
        self.build = self.document / "build"
        (self.build / "en").mkdir(parents=True)
        (self.build / "index.html").write_text("Chinese", encoding="utf-8")
        (self.build / "en/index.html").write_text("English", encoding="utf-8")
        self.bucket = Mock()
        self.sdk = types.SimpleNamespace(Auth=Mock(), Bucket=Mock(return_value=self.bucket))
        self.sdk_patch = patch.dict(sys.modules, {"oss2": self.sdk})
        self.path_patch = patch.object(deploy_to_oss, "DOCUMENT_DIR", self.document)
        self.sdk_patch.start()
        self.path_patch.start()
        self.addCleanup(self.sdk_patch.stop)
        self.addCleanup(self.path_patch.stop)

    def test_build_uses_document_directory(self):
        with patch.object(deploy_to_oss.subprocess, "run") as run:
            deploy_to_oss.build_project()
        run.assert_called_once_with(["npm", "run", "build"], cwd=self.document, check=True)

    def test_upload_preserves_locale_paths_and_content_type(self):
        deploy_to_oss.upload_to_oss(config_fixture())
        calls = self.bucket.put_object_from_file.call_args_list
        self.assertEqual(["en/index.html", "index.html"], [call.args[0] for call in calls])
        for call in calls:
            self.assertEqual({"Content-Type": "text/html"}, call.kwargs["headers"])
        self.sdk.Bucket.assert_called_once_with(self.sdk.Auth.return_value,
                                                "https://oss.example.test", "test-docs")

    def test_upload_failure_propagates(self):
        self.bucket.put_object_from_file.side_effect = RuntimeError("test-secret")
        with self.assertRaises(RuntimeError) as error:
            deploy_to_oss.upload_to_oss(config_fixture())
        self.assertEqual(1, self.bucket.put_object_from_file.call_count)
        self.assertNotIn("test-secret", str(error.exception))

    def test_upload_stays_inside_selected_prefix(self):
        deploy_to_oss.upload_to_oss({**config_fixture(), "prefix": "docs/hasor/"})
        self.assertEqual(["docs/hasor/en/index.html", "docs/hasor/index.html"],
                         [call.args[0] for call in self.bucket.put_object_from_file.call_args_list])

    def test_missing_build_is_rejected_before_upload(self):
        (self.build / "index.html").unlink()
        with self.assertRaises(FileNotFoundError):
            deploy_to_oss.upload_to_oss(config_fixture())
        self.sdk.Bucket.assert_not_called()

    def test_symlink_is_rejected_before_upload(self):
        (self.build / "linked.html").symlink_to(self.build / "index.html")
        with self.assertRaises(ValueError):
            deploy_to_oss.upload_to_oss(config_fixture())
        self.sdk.Bucket.assert_not_called()

    def test_build_failure_does_not_upload(self):
        with patch.object(deploy_to_oss, "deployment_lock", return_value=contextlib.nullcontext()), \
                patch.object(deploy_to_oss, "load_config", return_value=config_fixture()), \
                patch.object(deploy_to_oss, "build_project", side_effect=subprocess.CalledProcessError(1, "npm")), \
                patch.object(deploy_to_oss, "upload_to_oss") as upload, contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(1, deploy_to_oss.main())
        upload.assert_not_called()


class CdnRefreshTest(unittest.TestCase):
    def test_refresh_uses_configured_directory_and_reports_submission(self):
        client = Mock()
        client.do_action_with_exception.return_value = b'{"RefreshTaskId":"task-123"}'
        client_factory = Mock(return_value=client)
        request = Mock()
        sdk_modules = {"aliyunsdkcore.client": types.SimpleNamespace(AcsClient=client_factory),
                       "aliyunsdkcdn.request.v20180510": types.SimpleNamespace(
                           RefreshObjectCachesRequest=types.SimpleNamespace(RefreshObjectCachesRequest=Mock(return_value=request)))}
        output = io.StringIO()
        with patch.dict(sys.modules, sdk_modules), contextlib.redirect_stdout(output):
            refresh_website_cdn.refresh_cdn(config_fixture())
        client_factory.assert_called_once_with("test-id", "test-secret", "cn-hangzhou")
        request.set_ObjectPath.assert_called_once_with("https://docs.example.test/hasor/")
        request.set_ObjectType.assert_called_once_with("Directory")
        self.assertIn("task-123", output.getvalue())

    def test_refresh_failure_returns_nonzero(self):
        with patch.object(refresh_website_cdn, "deployment_lock", return_value=contextlib.nullcontext()), \
                patch.object(refresh_website_cdn, "load_config", return_value=config_fixture()), \
                patch.object(refresh_website_cdn, "refresh_cdn", side_effect=RuntimeError("unavailable")), \
                contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(1, refresh_website_cdn.main())


class DeploymentShellTest(unittest.TestCase):
    def test_steps_run_in_order_and_stop_on_each_failure(self):
        script = (Path(__file__).resolve().parents[1] / "deploy.sh").read_text(encoding="utf-8")
        steps = ["-m venv .deploy-venv", "-m pip install --disable-pip-version-check -r requirements-deploy.txt",
                 "deploy.py"]
        fake_python = '''#!/usr/bin/env bash
set -eu
printf '%s\\n' "$*" >> "$DEPLOY_TEST_LOG"
if [[ "$*" == "$DEPLOY_TEST_FAILURE" ]]; then
    exit 7
fi
if [[ "$*" == '-m venv .deploy-venv' ]]; then
    mkdir -p .deploy-venv/bin
    cp "$0" .deploy-venv/bin/python
fi
'''
        for failure in ["", *steps]:
            with self.subTest(failure=failure), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                document = root / "document with spaces"
                commands = root / "commands"
                document.mkdir()
                commands.mkdir()
                (document / "deploy.sh").write_text(script, encoding="utf-8")
                (commands / "python3").write_text(fake_python, encoding="utf-8")
                (commands / "python3").chmod(0o755)
                (commands / "npm").write_text("#!/bin/sh\nexit 0\n", encoding="utf-8")
                (commands / "npm").chmod(0o755)
                log = root / "steps.log"
                environment = {**os.environ, "PATH": str(commands) + os.pathsep + os.environ["PATH"],
                               "DEPLOY_TEST_LOG": str(log), "DEPLOY_TEST_FAILURE": failure}
                result = subprocess.run(["bash", str(document / "deploy.sh")], cwd=root,
                                        env=environment, capture_output=True, text=True)
                self.assertEqual(7 if failure else 0, result.returncode, result.stderr)
                expected = steps[:steps.index(failure) + 1] if failure else steps
                self.assertEqual(expected, log.read_text(encoding="utf-8").splitlines())


if __name__ == "__main__":
    unittest.main()
