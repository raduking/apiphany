#!/bin/sh

env DYLD_INSERT_LIBRARIES=./build/openssl_rand_override_mac.dylib \
	DYLD_FORCE_FLAT_NAMESPACE=1 \
	~/local/openssl-nonhardened/bin/openssl s_client -connect localhost:12345 -tls1_2 -servername localhost -msg -debug
