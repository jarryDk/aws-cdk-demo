#!/bin/sh
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Default profile --> lambda_user"
    PROFILE="--profile lambda_user"
else
    PROFILE="--profile $2"
fi

mvn clean package && cdk deploy --all --require-approval=never