package xebia.lcre;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import org.apache.log4j.Logger;
import xebia.lcre.build.failure.reason.BuildFailureReasonFinder;
import xebia.lcre.build.result.BuildResult;
import xebia.lcre.build.result.handlers.BuildResultHandler;
import xebia.lcre.build.spec.BuildSpecificationBuilder;
import xebia.lcre.pkg.creators.BuildPackage;
import xebia.lcre.pkg.creators.BuildPackageCreator;

public class BuildHandler implements RequestHandler<S3Event, String> {

    private static final Logger logger = Logger.getLogger(BuildHandler.class);

    private final AmazonS3Client s3Client = new AmazonS3Client();
    private final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient();

    private final BuildPackageCreator buildPackageCreator = new BuildPackageCreator(s3Client);
    private final BuildExecutor buildExecutor = BuildExecutor.newRunner(new BuildSpecificationBuilder());
    private final BuildFailureReasonFinder buildFailureReasonFinder = BuildFailureReasonFinder.newFinder();

    private final BuildResultHandler resultHandler = new BuildResultHandler(
            s3Client,
            dynamoDBClient,
            buildFailureReasonFinder
    );

    @Override
    public String handleRequest(S3Event event, Context context) {
        try {
            S3EventNotificationRecord record = event.getRecords().get(0);
            BuildPackage buildPackage = buildPackageCreator.create(record);
            logger.info(String.format("Working on BuildPackage >> %s", buildPackage));
            BuildResult buildResult = buildExecutor.execute(buildPackage);
            return resultHandler.handle(buildResult);
        } catch (Exception e) {
            logger.error("Failure occurred .. ", e);
            return "FAILED";
        }
    }


}
