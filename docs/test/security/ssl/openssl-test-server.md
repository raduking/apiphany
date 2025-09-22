
Start the OpenSSL server with AEAD - (Cipher suite: TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384)

```bash
openssl s_server \
  -accept 4433 \
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
```

Start the OpenSSL server with BLOCK - (Cipher suite: TLS_RSA_WITH_AES_128_CBC_SHA)

```bash
openssl s_server \
  -accept 4433 \
  -cert server.crt \
  -key server.key \
  -cipher AES128-SHA \
  -tls1_2 \
  -www \
  -debug \
  -msg \
  -state \
  -tlsextdebug \
  -trace
```
