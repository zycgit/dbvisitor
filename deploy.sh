#!/bin/bash

# Ensure we are in the script's directory (workspace root)
cd "$(dirname "$0")"

# Navigate to dbvisitor-doc directory where the python scripts and build artifacts are located
cd dbvisitor-doc || { echo "Error: dbvisitor-doc directory not found"; exit 1; }

echo "Checking Python environment..."

# Check if pip3 is installed
if ! command -v pip3 &> /dev/null; then
    echo "Error: pip3 is not installed. Please install Python3 and pip3 first."
    exit 1
fi

# Install required Python packages
echo "Installing/Updating required Python libraries..."
# oss2 for deploy_to_oss.py
# aliyun-python-sdk-core and aliyun-python-sdk-cdn for refresh_website_cdn.py
pip3 install oss2 aliyun-python-sdk-core aliyun-python-sdk-cdn --disable-pip-version-check

if [ $? -ne 0 ]; then
    echo "Error: Failed to install python dependencies."
    exit 1
fi

# Run the deployment script
echo "Starting deployment process..."
python3 deploy_to_oss.py

if [ $? -ne 0 ]; then
    echo "Error: Deployment failed."
    exit 1
fi

# Run the CDN refresh script
echo "Starting CDN refresh..."
python3 refresh_website_cdn.py

if [ $? -ne 0 ]; then
    echo "Error: CDN refresh failed."
    exit 1
fi

echo "Deployment and CDN refresh completed successfully."
