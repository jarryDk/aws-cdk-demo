#!/bin/bash

set -e

aws s3 ls s3://aws-cdk-demo-lamda-layers --profile lambda_user --region eu-central-1