#!/bin/sh

mkdir -p build

gcc -dynamiclib -o build/openssl_rand_override_mac.dylib src/openssl_rand_override_mac.c \
	-I$HOME/local/openssl-nonhardened/include \
	-L$HOME/local/openssl-nonhardened/lib \
	-lcrypto \
	-Wl,-undefined,dynamic_lookup
