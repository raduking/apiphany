#!/bin/sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

PORT=4433
OPENSSL_HOME="$HOME/local/openssl-nonhardened"

echo "Starting OpenSSL server with custom RAND override on macOS on port: $PORT"

env DYLD_INSERT_LIBRARIES="$PROJECT_DIR/build/openssl_rand_override_mac.dylib" \
	DYLD_FORCE_FLAT_NAMESPACE=1 \
	$OPENSSL_HOME/bin/openssl \
		s_server \
		-accept $PORT \
		-cert "$PROJECT_DIR/certs/server.crt" \
		-key "$PROJECT_DIR/certs/server.key" \
		-cipher ECDHE-RSA-AES256-GCM-SHA384 \
		-tls1_2 \
		-www \
		-debug \
		-msg \
		-state \
		-tlsextdebug \
		-trace
