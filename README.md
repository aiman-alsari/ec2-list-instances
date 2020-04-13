# ec2-list-instances

## Local Usage

Install the [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)

e.g. with homebrew:
```
brew install aws/tap/aws-sam-cli
```
Compile and copy the exploded jars to run locally
```
./gradlew build && ./gradlew exploded
```
Start the API server using SAM
```
sam local start-api --template sam-local.yaml
```

You can now test the API using curl at localhost:3000
```
curl -H 'aws-access-key-id: YOUR_ACCESS_KEY_ID' -H 'aws-access-key-secret: YOUR_ACCESS_KEY_SECRET' http://localhost:3000/instances/us-east-2/?sortField=publicIP | python -m json.tool
```

## Deploying to AWS Lambda

1. Create an S3 bucket to store the artifacts for deployment.
2. Package and upload the API
```
aws cloudformation package --template-file sam.yaml --output-template-file output-sam.yaml --s3-bucket YOUR_S3_BUCKET_NAME
```
3. Using the same region as your S3 Bucket, deploy the API
```
aws cloudformation deploy --template-file output-sam.yaml --stack-name EC2ListInstances --capabilities CAPABILITY_IAM --region=YOUR_REGION
```
4. Describe the stack to see the new URL of your API
```
aws cloudformation describe-stacks --stack-name EC2ListInstances --region=YOUR_REGION
```

## Improvements

As this was supposed to be a quick technical test, there are some shortcuts that I've taken here so as not to spend too long polishing this. If I was to spend more timeon this I would add the following:

* Add auto generated Swagger docs
* Change from using AWS basic credentials to temporary security credentials
* Make the sort more intelligent, currently does alphabetical sort only which is a bit dumb for IP addresses
* Use GraalVM to generate a native image thereby massively reducing the cold start time, unfortunately the [AWS SDK relies heavily on reflection etc](https://github.com/aws/aws-sdk-java/issues/2037) and it is not currently possible.
* Find a way to sort the full result set then paginate them, rather than the current behaviour which sorts just the current page. This is due to the AWS SDK not providing a sort option, so we would have to fetch all data, store it, sort it, then re-implement pagination.
