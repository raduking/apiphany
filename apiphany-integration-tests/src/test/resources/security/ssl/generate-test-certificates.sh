#!/bin/bash

# Generate a self-signed certificate for WireMock
keytool -genkeypair \
	-alias apiphany.server \
	-keystore server-keystore.jks \
	-storepass serverkeystorepass123 \
	-keypass serverkeystorepass123 \
	-dname "CN=localhost, OU=Test, O=Apiphany, L=New York, S=New York, C=US" \
	-keyalg RSA \
	-keysize 2048 \
	-validity 36500

# Export the server certificate
keytool -exportcert \
	-alias apiphany.server \
	-keystore server-keystore.jks \
	-storepass serverkeystorepass123 \
	-file server-cert.cer

# Create a truststore for clients
keytool -importcert \
	-alias apiphany.server \
	-keystore client-truststore.jks \
	-storepass clienttruststorepass123 \
	-file server-cert.cer \
	-noprompt

# Generate client certificate
keytool -genkeypair \
	-alias apiphany.client \
	-keystore client-keystore.jks \
	-storepass clientkeystorepass123 \
	-keypass clientkeystorepass123 \
	-dname "CN=test-client, OU=Test, O=Apiphany, L=New York, S=New York, C=US" \
	-keyalg RSA \
	-keysize 2048 \
	-validity 36500

# Export client certificate
keytool -exportcert \
	-alias apiphany.client \
	-keystore client-keystore.jks \
	-storepass clientkeystorepass123 \
	-file client-cert.cer

# Import client certificate to WireMock truststore
keytool -importcert \
	-alias apiphany.client \
	-keystore server-truststore.jks \
	-storepass servertruststorepass123 \
	-file client-cert.cer \
	-noprompt

echo "Certificates and keystores generated successfully."
echo .

# Verify the contents of the keystores and truststores

# List server keystore contents
keytool -list -v -keystore server-keystore.jks -storepass serverkeystorepass123

# List server truststore (should show client cert)
keytool -list -v -keystore server-truststore.jks -storepass servertruststorepass123

# List client keystore
keytool -list -v -keystore client-keystore.jks -storepass clientkeystorepass123

# List client truststore (should show server cert)
keytool -list -v -keystore client-truststore.jks -storepass clienttruststorepass123
