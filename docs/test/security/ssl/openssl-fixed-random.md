## Compile the random override (only works for Mac OS)

```bash
gcc -dynamiclib -o openssl_rand_override_mac.dylib openssl_rand_override_mac.c \
  -I$HOME/local/openssl-nonhardened/include \
  -L$HOME/local/openssl-nonhardened/lib \
  -lcrypto \
  -Wl,-undefined,dynamic_lookup
```

## Server

```bash
env DYLD_INSERT_LIBRARIES=./openssl_rand_override_mac.dylib \
  DYLD_FORCE_FLAT_NAMESPACE=1 \
  ~/local/openssl-nonhardened/bin/openssl s_server \
    -accept 4433 \
    -cert server.crt \
    -key server.key \
    -cipher ECDHE-RSA-AES256-GCM-SHA384 \
    -curves X25519 \
    -tls1_2 \
    -www \
    -debug \
    -msg \
    -state \
    -tlsextdebug \
    -trace
```

## Client

```bash
env DYLD_INSERT_LIBRARIES=./openssl_rand_override_mac.dylib \
  DYLD_FORCE_FLAT_NAMESPACE=1 \
  ~/local/openssl-nonhardened/bin/openssl s_client \
    -connect localhost:4433 \
    -cipher ECDHE-RSA-AES256-GCM-SHA384 \
    -curves X25519 \
    -tls1_2 \
    -servername localhost \
    -msg \
    -debug
    -msg \
    -state \
    -tlsextdebug \
    -trace
```
