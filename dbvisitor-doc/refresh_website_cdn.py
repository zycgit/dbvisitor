#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import json

try:
    from aliyunsdkcore.client import AcsClient
    from aliyunsdkcdn.request.v20180510 import RefreshObjectCachesRequest
except ImportError:
    print("Error: Aliyun SDK is not installed.")
    print("Please install it using: pip install aliyun-python-sdk-core aliyun-python-sdk-cdn")
    sys.exit(1)

# Configuration file name
CONFIG_FILE_NAME = ".dbvisitor-oss-config.json"

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

def refresh_cdn(config):
    access_key_id = config.get("accessKeyId")
    access_key_secret = config.get("accessKeySecret")
    cdn_region_id = config.get("cdnRegionId", "cn-hangzhou")
    cdn_url = config.get("cdnUrl")

    if not all([access_key_id, access_key_secret, cdn_url]):
        print("Error: Missing required configuration fields (accessKeyId, accessKeySecret, cdnUrl).")
        sys.exit(1)
    
    # Ensure cdn_url ends with / for directory refresh
    if not cdn_url.endswith('/'):
        cdn_url += '/'

    print(f"Initializing Aliyun Client (Region: {cdn_region_id})...")
    client = AcsClient(access_key_id, access_key_secret, cdn_region_id)

    print(f"Triggering CDN Refresh for directory: {cdn_url}")
    request = RefreshObjectCachesRequest.RefreshObjectCachesRequest()
    request.set_ObjectPath(cdn_url)
    request.set_ObjectType("Directory")

    try:
        response = client.do_action_with_exception(request)
        response_json = json.loads(str(response, encoding='utf-8'))
        print("Refresh request submitted successfully.")
        print(f"RefreshTaskId: {response_json.get('RefreshTaskId')}")
        print(f"RequestId: {response_json.get('RequestId')}")
    except Exception as e:
        print(f"Failed to refresh CDN: {e}")
        sys.exit(1)

def main():
    # Check if we are in the right directory (basic check)
    if not os.path.exists("package.json"):
        print("Warning: package.json not found in current directory. Make sure you are in 'dbvisitor-doc' root.")
    
    config = load_config()
    refresh_cdn(config)

if __name__ == "__main__":
    main()
