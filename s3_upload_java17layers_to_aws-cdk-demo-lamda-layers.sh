#!/bin/bash

set -e

aws s3 cp java17layer.zip s3://aws-cdk-demo-lamda-layers --profile lambda_user --region eu-central-1