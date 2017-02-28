# lambda-coding-round-evaluator
The goal of this project is to implement a code evaluator that organizations can use to automate coding round interviews. This is inspired by Coursera's Scala Progfun code evaluator. The project is built following Serverless architecture. The current implementation is based on AWS Lambda and Serverless framework.

Lambda functions are written in Java, Node.js, and Scala programming languages.

The project uses following AWS services:

1. AWS Lambda
2. AWS DynamoDB
3. AWS Cloudformation
4. AWS SES
5. AWS S3

## What is it?

`lambda-coding-round-evaluator` is used to automate coding round submission and evaluation. It helps you get rid of emails. Yay!

In my current organization, one of the interview rounds is a coding round. Candidate is emailed an assignment that he/she has to submit in a week time. The assignment is then evaluated by an existing employee who then makes the decision on whether candidate passed or failed the round. I wanted to automate this process so that we can filter out bad candidates without any human intervention. **A task that can be automated should be automated**. This is how the flow will work:

1. Recruitment team submit candidate details to the system.
2. System sends an email with assignment zip to the candidate based on candidate skills and experience. The zip contains the problem as well as a Gradle or Maven project.
3. Candidate writes the code and submits the assignment using Maven or Gradle task like `gradle submitAssignment`. The task zips the source code of the candidate and submits it to the system.
4. On receiving assignment, systems builds the project and run all test cases. 
   1. If the build fails, then candidate status is updated to failed in the system and recruitment team is notified. 
   2. If build succeeds, then we find the test code coverage and if it is less than a threshold then we mark candidate status to failed and recruitment team is notified.
5. If build succeeds and code coverage is above a threshold, then we run static analysis on the code to calculate the code quality score. If code quality score is below a threshold then candidate is marked failed and notification is sent to the recruitment team. Else, candidate passes the round and a human interviewer will now evaluate candidate assignment.

![](images/coding-round-evaluator.png)



## Services

This project is composed four services:

1. `candidate-service`: This service exposes REST API using API Gateway and Lambda that is used by the user interface to submit candidate details. The candidate details are stored in DynamoDB
2. `assignment-sender-service`: This service has one Lambda function that listens to DynamoDB stream and based on candidate experience and skills sends assignment to candidate via email.
3. `assignment-build-executor-service`: This service is invoked when candidate submits code using Gradle task. This builds the project and decides whether candidate passed or failed the test. 
4. `ui-service`: This exposes UI using Cloudfront and S3. 

## License

Apache. 



## Contact Me

You can follow me on twitter at [https://twitter.com/shekhargulati](https://twitter.com/shekhargulati) or email me at <shekhargulati84@gmail.com>. Also, you can read my blogs at [http://shekhargulati.com/](http://shekhargulati.com/).

