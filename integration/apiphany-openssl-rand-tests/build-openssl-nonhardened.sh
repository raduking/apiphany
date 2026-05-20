#!/bin/bash

# Exit immediately if any command fails
set -e

# Set install prefix and OpenSSL git repo
PREFIX="$HOME/local/openssl-nonhardened"
REPO="https://github.com/openssl/openssl.git"
BRANCH="openssl-3.5.1"

# Clean any previous build
rm -rf openssl-build
mkdir openssl-build
cd openssl-build

echo "--------------------------------------------"
echo "Cloning OpenSSL $BRANCH..."
echo "--------------------------------------------"
git clone --depth=1 --branch "$BRANCH" "$REPO" source
cd source

echo "--------------------------------------------"
echo "Configuring OpenSSL (non-hardened)..."
echo "--------------------------------------------"
./Configure darwin64-arm64-cc \
	shared \
	--prefix="${PREFIX}" \
	enable-ec_nistp_64_gcc_128

echo "--------------------------------------------"
echo "Building OpenSSL..."
echo "--------------------------------------------"
CORES=$(sysctl -n hw.ncpu)
make -j"$CORES"

echo "--------------------------------------------"
echo "Installing to ${PREFIX}..."
echo "--------------------------------------------"
make install

echo "--------------------------------------------"
echo "Installed OpenSSL to ${PREFIX}/bin/openssl"
