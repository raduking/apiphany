# Generate test certificate files

Generate `keystore.jks`

```bash
keytool -genkeypair \
    -alias io.github.raduking \
    -keyalg RSA \
    -keysize 2048 \
    -validity 36500 \
    -keystore keystore.jks \
    -storepass keystorepassword123 \
    -keypass keystorepassword123 \
    -dname "CN=localhost, OU=Development, O=RaduKing, L=New York, ST=New York, C=US"
```

Check the contents of the `keystore.jks`

```bash
keytool -list \
    -v \
    -keystore keystore.jks \
    -storepass keystorepassword123
```

Export the certificate from `keystore.jks`

```bash
keytool -exportcert \
    -alias io.github.raduking \
    -keystore keystore.jks \
    -storepass keystorepassword123 \
    -file mycertificate.cer
```

Create a `truststore.jks` and import the certificate

```bash
keytool -importcert \
    -alias io.github.raduking \
    -file mycertificate.cer \
    -keystore truststore.jks \
    -storepass truststorepassword123 \
    -noprompt
```

## Optional

Convert `keystore.jks` to `keystore.p12`

```bash
keytool -importkeystore \
  -srckeystore keystore.jks \
  -srcstorepass keystorepassword123 \
  -destkeystore keystore.p12 \
  -deststoretype PKCS12 \
  -deststorepass p12password123
```

Extract the private key (unencrypted)

```bash
openssl pkcs12 \
  -in keystore.p12 \
  -nocerts \
  -nodes \
  -out server.key \
  -password pass:p12password123
```

Extract the certificate

```bash
openssl pkcs12 \
  -in keystore.p12 \
  -clcerts \
  -nokeys \
  -out server.crt \
  -password pass:p12password123
```

Verify Key-Cert Match:

```bash
openssl x509 \
  -noout \
  -modulus \
  -in server.crt | openssl md5
```

```bash
openssl rsa \
  -noout \
  -modulus \
  -in server.key | openssl md5
```

# Keycloak certificate

```bash
openssl req \
  -new \
  -x509 \
  -key rsa_private.pem \
  -out rsa_certificate.pem \
  -days 36500 \
  -subj "/CN=apiphany-client-pk"
```

To add this to the `keycloak-realm-config.json` in the `..."jwt.credential.certificate"` field you will need a text editor to replace <b>every new line</b> with the actual string `"\n"`.
The new line might be different on Windows (`\r\n`) than on Linux/Mac OS (`\n`).

# OpenSSL Server

To start an OpenSSL server for testing see [openssl-test-server.md](openssl-test-server.md)
