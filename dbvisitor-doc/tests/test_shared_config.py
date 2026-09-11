"""Shared configuration, destination isolation and publication locking contracts."""

import contextlib
import copy
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import Mock, patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import deploy
import deploy_config
from deploy_site import SITE_NAME


def shared_fixture():
    return {"credentials": {"docs-publisher": {"accessKeyId": "test-id", "accessKeySecret": "test-secret"}},
            "sites": {name: {"credential": "docs-publisher", "endpoint": "oss.example.test",
                              "bucketName": name + "-docs", "prefix": "",
                              "cdnUrl": "https://" + name + ".example.test/"}
                      for name in ("dbvisitor", "hasor")}}


class SharedConfigurationTest(unittest.TestCase):
    def test_resolves_only_explicit_site_and_shared_credential(self):
        for site in ("hasor", "dbvisitor"):
            result = deploy_config.validate_config(shared_fixture(), site)
            self.assertEqual(site, result["siteName"])
            self.assertEqual(site + "-docs", result["bucketName"])
            self.assertEqual("test-id", result["accessKeyId"])
            self.assertEqual("https://oss.example.test", result["endpoint"])

    def test_separate_credentials_are_supported(self):
        fixture = shared_fixture()
        fixture["credentials"]["other"] = {"accessKeyId": "other-id", "accessKeySecret": "other-secret"}
        fixture["sites"]["hasor"]["credential"] = "other"
        self.assertEqual("other-id", deploy_config.validate_config(fixture, "hasor")["accessKeyId"])

    def test_missing_site_credential_and_legacy_format_are_rejected(self):
        missing_site = shared_fixture()
        del missing_site["sites"][SITE_NAME]
        missing_credential = shared_fixture()
        missing_credential["credentials"] = {}
        missing_secret = shared_fixture()
        missing_secret["credentials"]["docs-publisher"]["accessKeySecret"] = ""
        for fixture in (missing_site, missing_credential, missing_secret, {}, [], {"bucketName": "old-format"}):
            with self.subTest(fixture=type(fixture).__name__), self.assertRaises(ValueError):
                deploy_config.validate_config(fixture, SITE_NAME)

    def test_same_bucket_requires_non_overlapping_prefixes_even_with_different_endpoints(self):
        for first, second in (("", "hasor"), ("docs", "docs"), ("docs", "docs/hasor"), ("docs/hasor", "docs")):
            fixture = shared_fixture()
            fixture["sites"]["hasor"].update(bucketName="shared", prefix=first)
            fixture["sites"]["dbvisitor"].update(bucketName="shared", prefix=second, endpoint="https://internal.example.test")
            with self.subTest(prefixes=(first, second)), self.assertRaisesRegex(ValueError, "Overlapping OSS"):
                deploy_config.validate_config(fixture, SITE_NAME)

    def test_sibling_prefixes_and_lookalike_names_do_not_overlap(self):
        fixture = shared_fixture()
        fixture["sites"]["hasor"].update(bucketName="shared", prefix="/docs/hasor/")
        fixture["sites"]["dbvisitor"].update(bucketName="shared", prefix="docs/hasor-more")
        self.assertEqual("docs/hasor/", deploy_config.validate_config(fixture, "hasor")["prefix"])

    def test_cdn_overlap_rejected_even_when_buckets_differ(self):
        fixture = shared_fixture()
        fixture["sites"]["hasor"]["cdnUrl"] = "https://docs.example.test/"
        fixture["sites"]["dbvisitor"]["cdnUrl"] = "http://docs.example.test/dbvisitor/"
        with self.assertRaisesRegex(ValueError, "Overlapping CDN"):
            deploy_config.validate_config(fixture, SITE_NAME)

    def test_cdn_sibling_directories_are_supported(self):
        fixture = shared_fixture()
        fixture["sites"]["hasor"]["cdnUrl"] = "https://docs.example.test/hasor"
        fixture["sites"]["dbvisitor"]["cdnUrl"] = "https://docs.example.test/dbvisitor"
        self.assertEqual("https://docs.example.test/hasor/", deploy_config.validate_config(fixture, "hasor")["cdnUrl"])

    def test_unsafe_paths_and_urls_are_rejected(self):
        for prefix in ("../other", "docs/../other", "docs//other", "docs\\other", "docs/%2e%2e", None):
            fixture = shared_fixture()
            fixture["sites"][SITE_NAME]["prefix"] = prefix
            with self.subTest(prefix=prefix), self.assertRaises(ValueError):
                deploy_config.validate_config(fixture, SITE_NAME)
        for url in ("bad-url", "https://user:secret@example.test/", "https://example.test/?a=1", "https://example.test/#x", "https://example.test/../other"):
            fixture = shared_fixture()
            fixture["sites"][SITE_NAME]["cdnUrl"] = url
            with self.subTest(url=url), self.assertRaises(ValueError):
                deploy_config.validate_config(fixture, SITE_NAME)

    def test_shared_file_override_and_no_legacy_fallback(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(deploy_config.Path, "home", return_value=Path(directory)), \
                patch.dict(os.environ, {}, clear=True):
            root = Path(directory)
            for name in (".hasor-oss-config.json", ".dbvisitor-oss-config.json", "oss-config.json"):
                (root / name).write_text(json.dumps(shared_fixture()), encoding="utf-8")
            with self.assertRaises(ValueError):
                deploy_config.load_config()
            (root / deploy_config.CONFIG_FILE_NAME).write_text(json.dumps(shared_fixture()), encoding="utf-8")
            self.assertEqual(SITE_NAME, deploy_config.load_config()["siteName"])
            override = root / "explicit.json"
            configuration = shared_fixture()
            configuration["sites"][SITE_NAME]["bucketName"] = "explicit-target"
            override.write_text(json.dumps(configuration), encoding="utf-8")
            with patch.dict(os.environ, {deploy_config.CONFIG_ENV: str(override)}):
                self.assertEqual("explicit-target", deploy_config.load_config()["bucketName"])
            with patch.dict(os.environ, {deploy_config.CONFIG_ENV: str(root / "missing.json")}):
                with self.assertRaises(ValueError):
                    deploy_config.load_config()
            override.write_text('{"secret":test-secret', encoding="utf-8")
            with patch.dict(os.environ, {deploy_config.CONFIG_ENV: str(override)}):
                with self.assertRaises(ValueError) as error:
                    deploy_config.load_config()
                self.assertNotIn("test-secret", str(error.exception))

    def test_sample_contains_both_sites(self):
        sample = json.loads((Path(__file__).resolve().parents[1] / "oss-config.sample.json").read_text(encoding="utf-8"))
        self.assertEqual({"hasor", "dbvisitor"}, set(sample["sites"]))
        sample["credentials"]["docs-publisher"] = {"accessKeyId": "test", "accessKeySecret": "test"}
        self.assertEqual(SITE_NAME, deploy_config.validate_config(sample, SITE_NAME)["siteName"])


class PublicationTest(unittest.TestCase):
    def test_same_site_lock_rejected_and_other_site_allowed_then_lock_released(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(deploy_config.Path, "home", return_value=Path(directory)):
            with deploy_config.deployment_lock():
                with self.assertRaisesRegex(RuntimeError, "already running"):
                    with deploy_config.deployment_lock():
                        self.fail("Lock should not be reentered")
                with patch.object(deploy_config, "SITE_NAME", "other-site"):
                    with deploy_config.deployment_lock():
                        pass
            with deploy_config.deployment_lock():
                pass

    def test_lock_released_after_exception(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(deploy_config.Path, "home", return_value=Path(directory)):
            with self.assertRaises(ValueError):
                with deploy_config.deployment_lock():
                    raise ValueError("test")
            with deploy_config.deployment_lock():
                pass

    def test_pipeline_uses_one_configuration_and_stops_on_failure(self):
        for failed_step in (None, "build", "upload", "refresh"):
            calls = []
            configuration = deploy_config.validate_config(shared_fixture(), SITE_NAME)
            def step(name):
                def execute(*args):
                    calls.append(name)
                    if args:
                        self.assertIs(configuration, args[0])
                    if name == failed_step:
                        raise RuntimeError("test failure")
                return execute
            with self.subTest(failed_step=failed_step), \
                    patch.object(deploy, "deployment_lock", return_value=contextlib.nullcontext()), \
                    patch.object(deploy, "load_config", return_value=configuration) as load, \
                    patch.object(deploy, "build_project", side_effect=step("build")), \
                    patch.object(deploy, "upload_to_oss", side_effect=step("upload")), \
                    patch.object(deploy, "refresh_cdn", side_effect=step("refresh")), \
                    contextlib.redirect_stderr(io.StringIO()), contextlib.redirect_stdout(io.StringIO()):
                self.assertEqual(1 if failed_step else 0, deploy.main())
                expected = ["build", "upload", "refresh"]
                if failed_step:
                    expected = expected[:expected.index(failed_step) + 1]
                self.assertEqual(expected, calls)
                load.assert_called_once_with()

    def test_invalid_configuration_never_builds(self):
        with patch.object(deploy, "deployment_lock", return_value=contextlib.nullcontext()), \
                patch.object(deploy, "load_config", side_effect=ValueError("invalid")), \
                patch.object(deploy, "build_project") as build, contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(1, deploy.main())
        build.assert_not_called()


if __name__ == "__main__":
    unittest.main()
