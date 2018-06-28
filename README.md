# PDIPlugin-AWS-SNS
_Amazon Webservices Simple Notification Service Plugin for Pentaho Data Integration_

The AWS-SNS Plugin enables you to send Notifications from a PDI Transformation via AWS SNS 
to subscribed users.



## System Requirements

- Pentaho Data Integration 7.0 or above
- Copy following jar files to ${DI\_HOME}/lib
    - [Jackson Databind][jackson] (Version 2.9.3 or higher - current version needs to be replaced)
    - [Amazon Java SDK for SNS][aws-sdk] (Version 1.11.269 or higher)
    - [Joda Time][joda] (Version 2.9.9 or higher - current version needs to be replaced)



## Installation

**Using Pentaho Marketplace (recommended)**

1. In the Pentaho Marketplace find the AWS SQS Reader Plugin and click Install
2. Restart Spoon

**Manual Install**

1. Create ${DI\_HOME}/plugins/steps/AWS-SQS-Reader directory (if not exists)
2. Copy the AWS-SQS-Reader-Plugin jar into the ${DI\_HOME}/plugins/steps/AWS-SQS-Reader directory
3. Restart Spoon



## Manual build

To build (with Apache Maven):

```shell
mvn package
```



## Documentation

For detailled information on how to configure and use the plugin please have a look at the [Documentation](https://freddyffm.github.io/PDIPlugin-AWS-SQS-Reader/).



## About Amazon SNS

[Amazon Simple Queue Service (Amazon SQS)][sqs] s a fully managed message queuing service that enables you to decouple and scale microservices, distributed systems, and serverless applications. SQS eliminates the complexity and overhead associated with managing and operating message oriented middleware, and empowers developers to focus on differentiating work. Using SQS, you can send, store, and receive messages between software components at any volume, without losing messages or requiring other services to be available. Get started with SQS in minutes using the AWS console, Commmand Line Interface or SDK of your choice, and three simple commands.

SQS offers two types of message queues. Standard queues offer maximum throughput, best-effort ordering, and at-least-once delivery. SQS FIFO queues are designed to guarantee that messages are processed exactly once, in the exact order that they are sent.



## Author

- [Michael Fraedrich](https://github.com/FreddyFFM/)



[sqs]: https://aws.amazon.com/sqs/
[jackson]: https://github.com/FasterXML/jackson-databind/wiki
[aws-sdk]: https://aws.amazon.com/de/sdk-for-java/
[joda]: https://github.com/JodaOrg/joda-time/releases