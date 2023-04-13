#!/bin/bash
set -eo pipefail
STACK_NAME=$(cat stack-name.txt)
FUNCTION=$(aws cloudformation describe-stack-resource --stack-name $STACK_NAME --logical-resource-id function --query 'StackResourceDetail.PhysicalResourceId' --output text)

while true; do
  aws lambda invoke --cli-binary-format raw-in-base64-out --function-name $FUNCTION --payload file://event.json out.json
  cat out.json
  echo ""
  sleep 2
done
