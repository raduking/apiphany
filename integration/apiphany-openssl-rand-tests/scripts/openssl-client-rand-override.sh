#!/bin/sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

PORT=12345
OPENSSL_HOME="$HOME/local/openssl-nonhardened"

env DYLD_INSERT_LIBRARIES="$PROJECT_DIR/build/openssl_rand_override_mac.dylib" \
	DYLD_FORCE_FLAT_NAMESPACE=1 \
	$OPENSSL_HOME/bin/openssl s_client -connect localhost:$PORT -tls1_2 -servername localhost -msg -debug
