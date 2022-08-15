#!/bin/sh
set -e

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Default profile --> lambda_user"
    PROFILE="--profile lambda_user"
else
    PROFILE="--profile $2"
fi

echo "Please deplay the layer aws-cdk-demo-java17layer too"
export DO_LAYER_DEPLOYMENT=true

echo "building functions"
mvn clean package

echo "building CDK"
cd cdk && mvn clean package && cdk deploy --require-approval=never $PROFILE

export DO_LAYER_DEPLOYMENT=false