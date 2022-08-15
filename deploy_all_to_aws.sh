#!/bin/bash

set -e

declare -a PROJECTS=(
    "aws-cdk-demo-simple-lambda"
    "aws-cdk-demo-hello-world-http"
    "aws-cdk-demo-hello-world-rest"
    "aws-cdk-demo-dynamodb-todo"
    "aws-cdk-demo-dynamodb-todo-jdk17"
    "aws-cdk-demo-dynamodb-todo-jdk17-alb"
    )

echo "Move to the folder aws-cdk-demo"
cd aws-cdk-demo

echo "Deploy table, role and bucket"
./buildAndDeployDontAsk.sh --profile lambda_user

echo "Move back to the root folder"
cd ..

echo "Upload java17layer.zip to the bucket aws-cdk-demo-lamda-layers"
aws s3 cp java17layer.zip s3://aws-cdk-demo-lamda-layers --profile lambda_user --region eu-central-1

echo "Set DO_LAYER_DEPLOYMENT to True"
export DO_LAYER_DEPLOYMENT=True

echo "Move to the folder aws-cdk-demo"
cd aws-cdk-demo
echo "Deploy table, role, bucket and java17 layer"
./buildAndDeployDontAsk.sh --profile lambda_user

echo "Move back to the root folder"
cd ..

echo "Set DO_LAYER_DEPLOYMENT to False"
export DO_LAYER_DEPLOYMENT=False

for val in ${PROJECTS[@]}; do
   echo "Move to the folder $val"
   cd $val

   echo "Deploy app $val"
   ./buildAndDeployDontAsk.sh --profile lambda_user

   echo "Move back to the root folder"
   cd ..

done

printf "\U1F984 \033[32mAll have been deployed to AWS!\033[39m"