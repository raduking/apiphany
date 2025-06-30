Generate `keystore.jks`

```
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

```
keytool -list \
    -v \
    -keystore keystore.jks \
    -storepass keystorepassword123
```

Export the certificate from `keystore.jks`

```
keytool -exportcert \
    -alias io.github.raduking \
    -keystore keystore.jks \
    -storepass keystorepassword123 \
    -file mycertificate.cer
```

Create a `truststore.jks` and import the certificate

```
keytool -importcert \
    -alias io.github.raduking \
    -file mycertificate.cer \
    -keystore truststore.jks \
    -storepass truststorepassword123 \
    -noprompt
```
