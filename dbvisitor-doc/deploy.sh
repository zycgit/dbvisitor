#!/bin/bash

# Ensure we are in the script's directory
cd "$(dirname "$0")"

echo "Checking Python environment..."

# Check if pip3 is installed
if ! command -v pip3 &> /dev/null; then
    echo "Error: pip3 is not installed. Please install Python3 and pip3 first."
    exit 1
fi

# Install required Python packages
echo "Installing/Updating required Python libraries..."
pip3 install oss2 --disable-pip-version-check

if [ $? -ne 0 ]; then
    echo "Error: Failed to install python dependencies."
    exit 1
fi

# Run the deployment script
echo "Starting deployment process..."
python3 deploy_to_oss.py
