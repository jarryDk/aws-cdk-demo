#!/bin/bash

set -e

if [ -z "$1" ] ; then
    echo "Default endpoint : http://localhost:8080/todos"
    ENDPOINT=http://localhost:8080/todos
else
    echo "Endpoint : $1"
    ENDPOINT=$1
fi

TIMESTAMP=`date +%Y-%m-%dT%H:%M:%S:%3N`

time curl -X POST $ENDPOINT \
	-H 'Accept: application/json' \
	-H 'Content-Type: application/json' \
	-d '{"subject":"Hello from Quarkus","body":"Content - Timestamp : '$TIMESTAMP'"}' | jq