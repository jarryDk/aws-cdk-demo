#!/bin/bash

set -e

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Default profile --> lambda_user"
    PROFILE="--profile lambda_user"
else
    PROFILE="--profile $2"
fi

cd cdk && cdk destroy $PROFILE