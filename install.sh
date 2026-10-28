#!/bin/sh

# Exit immediately if the build fails.
set -e

# Install all modules with Maven while redirecting test output to the test reports.
mvn clean install -Dmaven.test.redirectTestOutputToFile=true

