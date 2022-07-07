#!/bin/bash

set -e

aws s3 rm s3://aws-cdk-demo-lamda-layers/java17layer.zip --profile lambda_user --region eu-central-1