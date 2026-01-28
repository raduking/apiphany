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

mvn deploy -Drelease=true \
	-pl apiphany-core \
	-am \
	-DskipTests

echo "Deployment completed successfully."
