#!/bin/sh

PORT=4433
echo "Starting OpenSSL server with custom RAND override on macOS on port: $PORT"

env DYLD_INSERT_LIBRARIES=./build/openssl_rand_override_mac.dylib \
	DYLD_FORCE_FLAT_NAMESPACE=1 \
	~/local/openssl-nonhardened/bin/openssl \
		s_server \
		-accept $PORT \
		-cert server.crt \
		-key server.key \
		-cipher ECDHE-RSA-AES256-GCM-SHA384 \
		-tls1_2 \
		-www \
		-debug \
		-msg \
		-state \
		-tlsextdebug \
		-trace
