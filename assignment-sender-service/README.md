assignment-sender-service
----

This is Scala based Serverless component. It is built using Serverless framework `aws-scala-sbt` template.

To deploy this project, you have to do following:

```
$ sbt clean assembly
$ serverless deploy -v
```