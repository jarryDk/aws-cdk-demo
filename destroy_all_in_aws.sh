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

for val in ${PROJECTS[@]}; do
   echo "Move to the folder $val"
   cd $val

   echo "Destroy app $val"
   ./destroy.sh --profile lambda_user

   echo "Move back to the root folder"
   cd ..

done

echo "Remove java17layer.zip from the bucket aws-cdk-demo-lamda-layers"
aws s3 rm s3://aws-cdk-demo-lamda-layers/java17layer.zip --profile lambda_user --region eu-central-1

echo "Move to the folder aws-cdk-demo"
cd aws-cdk-demo

echo "Destroy all created from the folder aws-cdk-demo"
./destroy.sh --profile lambda_user

printf "\U1F984 \033[32mAll have been removed from AWS!\033[39m"