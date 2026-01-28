#!/bin/sh
mvn clean deploy -Drelease=true -Dtest.tls.chunked=true
