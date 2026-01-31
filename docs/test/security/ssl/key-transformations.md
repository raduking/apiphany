
# To generate XDH keys (X25519 / X448):

```bash
openssl genpkey \
    -algorithm X25519 \
    -out xdh_private.key
```

```bash
openssl pkey \
    -in xdh_private.key \
    -pubout \
    -out xdh_public.key
```

# To generate RSA keys:

```bash
openssl genpkey \
    -algorithm RSA \
    -out rsa_private.pem \
    -pkeyopt rsa_keygen_bits:2048
```

```bash
openssl rsa \
    -in rsa_private.pem \
    -pubout \
    -out rsa_public.pem
```

# Convert public PEM to DER format

Note: this will not be a valid X.509 certificate, just the public key in DER format.

If the key starts with `-----BEGIN RSA PUBLIC KEY-----`, use the following command:

```bash
openssl rsa \
    -RSAPublicKey_in \
    -in rsa_public.pem \
    -outform DER \
    -out rsa_public.der
```

If the key starts with `-----BEGIN PUBLIC KEY-----`, use this command instead:

```bash
openssl pkey \
    -pubin \
    -in public.pem \
    -outform DER \
    -out public.der
```

Verify DER file:

```bash
openssl rsa \
    -inform DER \ 
    -in rsa_public.der \
    -pubin \
    -text \
    -noout
```

## Valid X.509 Certificate in DER format from PEM:

```bash
openssl req -new -x509 \
    -key rsa_private.pem \
    -out rsa_certificate.pem \
    -days 36500 \
    -subj "/CN=TLS Test"
```

```bash
openssl x509 \
    -in rsa_certificate.pem \
    -outform DER \
    -out rsa_certificate.der
```
