# ec2-list-instances

- [Local Usage](#local-usage)
- [API Reference](#api-reference)
  - [List EC2 Instances in a region](#list-ec2-instances-in-a-region)
  - [Success Response](#success-response)
- [Deploying to AWS Lambda](#deploying-to-aws-lambda)
- [Improvements](#improvements)

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

## API Reference

### List EC2 Instances in a region

Get basic details of the instances in a particular AWS region.

**URL** : `/instances/:region/`

**Method** : `GET`

**Auth Headers required** : `aws-access-key-id` and `aws-access-key-secret`

**Permissions required** : The access key needs the IAM policy `AmazonEC2ReadOnlyAccess`

**Optional Query Parameters**

|Field|Description|Example|
|---|---|---|
|**maxResults**|The maximumum number of results to return per page, default 6|`?maxResults=50`|
|**nextToken** | The token used to request the next page of results| `?nextToken=eyJ2IjoiM...`
|**sortField**|The field to sort on, must be one of `id`, `privateIP`, `publicIP`, `state`, or `type`. Defaults to sorting by `id`|`?sortField=state`
|**sortAscending**| Defaults to `true`, set to `false` to get descending sort order|`?sortField=state&sortAscending=false`


### Success Response

**Code** : `200 OK`

**Content examples**

For a single result with no further results.

```json
{
    "count": 1,
    "instances": [
        {
            "id": "i-0069d953f5c1ba763",
            "privateIP": "172.31.27.253",
            "publicIP": "18.223.98.239",
            "state": "running",
            "type": "t2.micro"
        }
    ]
}
```

When the max results per page (default of 6) is hit, a nextToken is returned which can be used to get the next page.

```json
{
    "count": 6,
    "instances": [
        {
            "id": "i-0069d953f5c1ba763",
            "privateIP": "172.31.27.253",
            "publicIP": "18.223.98.239",
            "state": "running",
            "type": "t2.micro"
        },
        {
            "id": "i-013c8c97c29063c8b",
            "privateIP": "172.31.28.84",
            "publicIP": "3.16.54.142",
            "state": "running",
            "type": "t2.micro"
        },
        {
            "id": "i-045c03a9f8f8e62c1",
            "privateIP": "172.31.28.0",
            "publicIP": "18.224.149.163",
            "state": "running",
            "type": "t2.micro"
        },
        {
            "id": "i-0a98d28abf39b5e17",
            "privateIP": "172.31.17.14",
            "publicIP": "3.17.158.188",
            "state": "running",
            "type": "t2.micro"
        },
        {
            "id": "i-0d561ce9d1e5bd5ad",
            "privateIP": "172.31.17.89",
            "publicIP": "13.58.98.178",
            "state": "running",
            "type": "t2.micro"
        },
        {
            "id": "i-0f3503651aad4996c",
            "privateIP": "172.31.26.59",
            "publicIP": "3.135.18.9",
            "state": "running",
            "type": "t2.micro"
        }
    ],
    "nextToken": "eyJ2IjoiMiIsImMiOiIwUEs0cmxoRDBzdVNDcm4wR0NOelZJNGZJWUdaSHhGY2MwMGZTSHd0WnBqTnJmVTdGaExNVkFMU3cyQXhINlhtSmllaVJleTd6Z0czcGNoc0NqeGRTNkpDcm0wSTZLT3AyU0duT1FTYnJteFFMYUZRd1JJNTRFeTFJRG5DN2hDK2k2Uk9PM280aEszd0dOSlRhQWtlTStPRU9WcEE0VDBSaG41S3Nmait0dlBENkNGMFFGL09qa1JwYXZXK3oyamF6bTJpeEE9PSIsInMiOiIxIn0="
}

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

As this was supposed to be a quick technical exercise, there are some shortcuts that I've taken here so as not to spend too long polishing this. If I was to spend more time on this I would add the following:

* Add auto generated Swagger docs
* Better error handling and more verbose logging, for instance a lot of AWS SDK exceptions are passed on as is rather than making them user friendly.
* Change from using AWS basic credentials to temporary security credentials
* Make the sort more intelligent, currently does alphabetical sort only which is a bit dumb for IP addresses
* Use GraalVM to generate a native image thereby massively reducing the cold start time, unfortunately the [AWS SDK relies heavily on reflection etc](https://github.com/aws/aws-sdk-java/issues/2037) and it is not currently possible.
* Find a way to sort the full result set then paginate them, rather than the current behaviour which sorts just the current page. This is due to the AWS SDK not providing a sort option, so we would have to fetch all data, store it, sort it, then re-implement pagination.
