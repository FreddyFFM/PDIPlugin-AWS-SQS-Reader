# PDIPlugin-AWS-SQS-Reader
_Amazon Webservices Simple Queue Service Plugin for Pentaho Data Integration_

The AWS-SQS Plugin enables you receive messages from Amazon Webservices Simple Queue Service within a PDI Transformation.



## System Requirements

- Pentaho Data Integration 7.1 or above



## Installation

**Using Pentaho Marketplace (recommended)**

1. In the Pentaho Marketplace find the AWS SQS Reader Plugin and click Install
2. Restart Spoon

**Manual Install**

1. Copy the AWS-SQS-Reader-Plugin.zip into the ${DI\_HOME}/plugins/steps/ directory and extract
2. Restart Spoon



## Manual build

To build (with Apache Maven):

```shell
mvn package
```



## Documentation

For detailled information on how to configure and use the plugin please have a look at the [Documentation](https://freddyffm.github.io/PDIPlugin-AWS-SQS-Reader/).



## About Amazon SQS

[Amazon Simple Queue Service (Amazon SQS)][sqs] s a fully managed message queuing service that enables you to decouple and scale microservices, distributed systems, and serverless applications. SQS eliminates the complexity and overhead associated with managing and operating message oriented middleware, and empowers developers to focus on differentiating work. Using SQS, you can send, store, and receive messages between software components at any volume, without losing messages or requiring other services to be available. Get started with SQS in minutes using the AWS console, Commmand Line Interface or SDK of your choice, and three simple commands.

SQS offers two types of message queues. Standard queues offer maximum throughput, best-effort ordering, and at-least-once delivery. SQS FIFO queues are designed to guarantee that messages are processed exactly once, in the exact order that they are sent.



## Author

- [Michael Fraedrich](https://github.com/FreddyFFM/)



[sqs]: https://aws.amazon.com/sqs/