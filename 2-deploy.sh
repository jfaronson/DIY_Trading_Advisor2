#!/bin/bash
set -eo pipefail
PROJECT=$(cat project-name.txt)
STACK_NAME=$(cat stack-name.txt)
ARTIFACT_BUCKET=$(cat bucket-name.txt)
TEMPLATE=template.yml
gradle build -i
mv build/distributions/$PROJECT-*-snapshot.zip build/$STACK_NAME.zip

aws cloudformation package --template-file $TEMPLATE --s3-bucket $ARTIFACT_BUCKET --output-template-file out.yml
aws cloudformation deploy --template-file out.yml --stack-name $STACK_NAME --capabilities CAPABILITY_NAMED_IAM
