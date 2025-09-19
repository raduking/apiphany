#!/bin/sh
mvn deploy -Drelease=true -Dtest.tls.chunked=true
