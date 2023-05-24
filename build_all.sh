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

cd aws-cdk-demo
echo "Build aws-cdk-demo"
mvn clean package

echo "Build aws-cdk-demo -> cdk"
cd cdk
mvn clean package
echo "Move back to project folder"
cd ..

echo "Move back to the root folder"
cd ..


for val in ${PROJECTS[@]}; do
   echo "Move to the folder $val"
   cd $val

   echo "Build app $val"
   mvn clean package

   echo "Build app $val -> cdk"
   cd cdk
   mvn clean package

   echo "Move back to project folder"
   cd ..

   echo "Move back to the root folder"
   cd ..

done

printf "\U1F984 \033[32mAll have been Build!\033[39m"