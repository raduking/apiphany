#!/bin/sh

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <new-version>"
    exit 1
fi

NEW_VERSION=$1
echo "Updating project version to $NEW_VERSION"

mvn versions:set -DnewVersion=$NEW_VERSION
mvn versions:update-child-modules
mvn versions:commit

