#!/bin/bash

S3_BUCKET_SIMPLE_NAME=$1
STACK_NAME=$2

if [ -z "$1" ] || [ -z "$2" ]
then
 echo "Missing input : S3_BUCKET_SIMPLE_NAME STACK_NAME"
fi

sam package --template-file target/sam.jvm.yaml --output-template-file package.yaml --s3-bucket $S3_BUCKET_SIMPLE_NAME --region eu-central-1 --profile lambda_user
sam deploy --template-file package.yaml --capabilities CAPABILITY_IAM --stack-name $STACK_NAME --region eu-central-1  --profile lambda_user