#!/bin/sh
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Default profile --> lambda_user"
    PROFILE="--profile lambda_user"
else
    PROFILE="--profile $2"
fi

echo "building functions"
mvn clean package

echo "building CDK"
cd cdk && mvn clean package && cdk deploy $PROFILE