#!/bin/sh

# Exit immediately if a command exits with a non-zero status
set -e

# Set the Maven binary folder, defaulting to the directory of the 'mvn' command if not already set
MAVEN_BIN_FOLDER="${MAVEN_BIN_FOLDER:-$(dirname "$(which mvn)")}"

# Run full build with tests
"$MAVEN_BIN_FOLDER/mvn" clean verify -Dtest.tls.chunked=true

# If we get here, all tests passed
echo "All tests passed. Deploying apiphany module..."

# -pl to specify the module to build (project list)
# -am to also build any dependencies (also make)
# -DskipTests to skip tests during deployment

"$MAVEN_BIN_FOLDER/mvn" deploy -Drelease=true \
	-pl apiphany-core,apiphany-httpclient5,apiphany-spring \
	-am \
	-DskipTests

echo "Deployment completed successfully."
