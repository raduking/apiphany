#!/bin/sh

# Exit immediately if a command exits with a non-zero status
set -e

# Run full build with tests
mvn clean verify -Dtest.tls.chunked=true

# If we get here, all tests passed
echo "All tests passed. Deploying apiphany module..."

# -pl to specify the module to build (project list)
# -am to also build any dependencies (also make)
# -DskipTests to skip tests during deployment

# Deploy the apiphany-core module to Maven Central using the central-publishing plugin
# We do not use 'mvn deploy' directly to ensure proper signing and publishing steps are followed

mvn -Drelease=true central-publishing:publish \
	-pl apiphany-core \
	-am \
	-DskipTests

echo "Deployment completed successfully."
