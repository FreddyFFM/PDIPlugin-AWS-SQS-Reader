# Installation

## System Requirements

- Pentaho Data Integration 7.1 or above (Pentaho 8.X is recommended!)
- Copy following jar files to ${DI\_HOME}/lib
    - [Jackson Databind][jackson] (Version 2.9.8 or higher - current version needs to be replaced)
    - [Amazon Java SDK][aws-sdk] (Version 1.11.269 or higher)
    - [Joda Time][joda] (Version 2.9.9 or higher - current version needs to be replaced)
- For Pentaho DI 7.X following files need to be replaced/added in ${DI\_HOME}/lib also
    - [Jackson Annotations][jackson] (Version 2.9.8 or higher - current version needs to be replaced)
    - [Jackson Core][jackson] (Version 2.9.8 or higher - current version needs to be replaced)
    - [Apache HttpCore][http] (Version 4.4.6 or higher)
    - [Apache HttpClient][http] (Version 4.5.3 or higher)


## Installation

**Using Pentaho Marketplace (recommended)**

1. In the Pentaho Marketplace find the AWS SQS Reader Plugin and click Install
2. Restart Spoon

For further information please do also have a look at the [readme.md](https://github.com/FreddyFFM/PDIPlugin-AWS-SQS-Reader/blob/master/README.md)


[jackson]: https://github.com/FasterXML/jackson-databind/wiki
[aws-sdk]: https://aws.amazon.com/de/sdk-for-java/
[joda]: https://github.com/JodaOrg/joda-time/releases
[http]: https://hc.apache.org/downloads.cgi