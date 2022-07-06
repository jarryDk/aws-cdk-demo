#!/bin/bash

set -e

if [ -z "$1" ] ; then
    echo "Default endpoint : https://localhost/todos"
    ENDPOINT=https://localhost/todos
else
    echo "Endpoint : $1"
    ENDPOINT=$1
fi

time curl -X POST $ENDPOINT \
	-H 'Accept: application/json' \
	-H 'Content-Type: application/json' \
	-d '{"subject":"Hello from Quarkus","body":"Content"}' | jq