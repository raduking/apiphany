#!/bin/bash

# Exit immediately if any command fails
set -e

# Set install prefix and OpenSSL git repo
PREFIX="$HOME/local/openssl-nonhardened"
REPO="https://github.com/openssl/openssl.git"
BRANCH="openssl-3.5.1"

echo "--------------------------------------------"
echo "Building OpenSSL (non-hardened) from source..."
echo "--------------------------------------------"
# Create build directory if it doesn't exist
mkdir -p openssl-build
cd openssl-build

# Clone only if source directory doesn't exist
if [ ! -d "source" ]; then
    echo "--------------------------------------------"
    echo "Cloning OpenSSL $BRANCH..."
    echo "--------------------------------------------"
    git clone --depth=1 --branch "$BRANCH" "$REPO" source
else
    echo "--------------------------------------------"
    echo "Source directory already exists, using existing clone..."
    echo "--------------------------------------------"
fi
cd source

echo "--------------------------------------------"
echo "Configuring OpenSSL (non-hardened)..."
echo "--------------------------------------------"
./Configure darwin64-arm64-cc \
	shared \
	--prefix="${PREFIX}" \
	enable-ec_nistp_64_gcc_128

CORES=$(sysctl -n hw.ncpu)
echo "--------------------------------------------"
echo "Building OpenSSL ($CORES cores)..."
echo "--------------------------------------------"
make -j"$CORES"

echo "--------------------------------------------"
echo "Installing to ${PREFIX}..."
echo "--------------------------------------------"
make install

echo "--------------------------------------------"
echo "Installed OpenSSL to ${PREFIX}/bin/openssl"
