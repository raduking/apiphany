#!/bin/sh

# Exit immediately if a command exits with a non-zero status
set -e

# Run full build with tests
mvn clean verify -Dtest.tls.chunked=true

# If we get here, all tests passed
echo "All tests passed. Deploying apiphany module..."

mvn deploy -Drelease=true \
	-pl apiphany \
	-am \
	-DskipTests

echo "Deployment completed successfully."
