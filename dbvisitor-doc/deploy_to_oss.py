#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import json
import subprocess
import mimetypes

try:
    import oss2
except ImportError:
    print("Error: 'oss2' library is not installed.")
    print("Please install it using: pip install oss2")
    sys.exit(1)

# Configuration file name
CONFIG_FILE_NAME = ".dbvisitor-oss-config.json"
BUILD_DIR = "build"

def load_config():
    # 1. Try to load from User Home Directory
    home_config = os.path.join(os.path.expanduser("~"), CONFIG_FILE_NAME)
    if os.path.exists(home_config):
        print(f"Loading configuration from: {home_config}")
        with open(home_config, 'r', encoding='utf-8') as f:
            return json.load(f)
    
    # 2. Try to load from current directory (optional, for testing)
    local_config = os.path.join(os.getcwd(), "oss-config.json")
    if os.path.exists(local_config):
        print(f"Loading configuration from: {local_config}")
        with open(local_config, 'r', encoding='utf-8') as f:
            return json.load(f)

    print(f"Error: Configuration file not found at ~/{CONFIG_FILE_NAME}")
    print("Please create the configuration file based on oss-config.sample.json.")
    sys.exit(1)

def build_project():
    print("Executing 'npm run build'...")
    try:
        # Use shell=True for Windows compatibility if needed, or list args
        # On macOS/Linux, invoking npm directly usually works if in PATH.
        subprocess.run(["npm", "run", "build"], check=True)
        print("Build successful.")
    except subprocess.CalledProcessError as e:
        print("Build failed.")
        sys.exit(1)
    except FileNotFoundError:
        print("Error: 'npm' command not found. Please ensure Node.js is installed and in PATH.")
        sys.exit(1)

def upload_to_oss(config):
    access_key_id = config.get("accessKeyId")
    access_key_secret = config.get("accessKeySecret")
    endpoint = config.get("endpoint")
    bucket_name = config.get("bucketName")

    if not all([access_key_id, access_key_secret, endpoint, bucket_name]):
        print("Error: Missing required configuration fields (accessKeyId, accessKeySecret, endpoint, bucketName).")
        sys.exit(1)

    print(f"Connecting to OSS Bucket: {bucket_name}...")
    auth = oss2.Auth(access_key_id, access_key_secret)
    bucket = oss2.Bucket(auth, endpoint, bucket_name)

    # Walk through the build directory
    build_path = os.path.join(os.getcwd(), BUILD_DIR)
    
    if not os.path.exists(build_path):
        print(f"Error: Build directory '{build_path}' does not exist.")
        sys.exit(1)
    
    print(f"Uploading files from {build_path}...")
    
    success_count = 0
    fail_count = 0

    for root, dirs, files in os.walk(build_path):
        for filename in files:
            local_file = os.path.join(root, filename)
            # Calculate object name (relative path in OSS)
            # e.g., build/index.html -> index.html
            # build/assets/css/style.css -> assets/css/style.css
            rel_path = os.path.relpath(local_file, build_path)
            # Ensure forward slashes for OSS paths even on Windows
            oss_key = rel_path.replace(os.path.sep, '/')
            
            # Guess content type
            content_type = mimetypes.guess_type(local_file)[0]
            headers = {}
            if content_type:
                headers['Content-Type'] = content_type

            # Optional: Add Cache-Control/other headers if needed
            
            try:
                print(f"Uploading: {oss_key}")
                bucket.put_object_from_file(oss_key, local_file, headers=headers)
                success_count += 1
            except Exception as e:
                print(f"Failed to upload {oss_key}: {e}")
                fail_count += 1

    print("-" * 30)
    print(f"Upload complete.")
    print(f"Success: {success_count}")
    print(f"Failed:  {fail_count}")

def main():
    # Check if we are in the right directory (basic check)
    if not os.path.exists("package.json"):
        print("Warning: package.json not found in current directory. Make sure you are in 'dbvisitor-doc' root.")
    
    config = load_config()
    build_project()
    upload_to_oss(config)

if __name__ == "__main__":
    main()
