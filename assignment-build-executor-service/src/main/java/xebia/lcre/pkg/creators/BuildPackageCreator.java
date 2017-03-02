package xebia.lcre.pkg.creators;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.log4j.Logger;
import xebia.lcre.exceptions.UnableToCreateBuildPackage;
import xebia.lcre.utils.ZipHelpers;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;

public class BuildPackageCreator {

    private final AmazonS3Client client;
    private final Logger logger = Logger.getLogger(BuildPackageCreator.class);

    public BuildPackageCreator(AmazonS3Client client) {
        this.client = client;
    }

    public BuildPackage create(S3EventNotificationRecord record) throws UnableToCreateBuildPackage {
        try {
            S3Entity entity = record.getS3();
            String srcKey = URLDecoder.decode(entity.getObject().getKey().replace('+', ' '), "UTF-8");
            S3Object s3Object = client.getObject(new GetObjectRequest(entity.getBucket().getName(), srcKey));
            String zipName = s3Object.getKey();
            Path zipPath = ZipHelpers.downloadZipTo(s3Object.getObjectContent(), zipName, "/tmp");
            return new BuildPackage(zipName, zipPath, extractCandidateId(zipPath));
        } catch (IOException e) {
            logger.error("Unable to create build package", e);
            throw new UnableToCreateBuildPackage("Unable to create build package", e);
        }
    }

    private String extractCandidateId(Path zipPath) {
        return zipPath.getFileName().toString().replace("assignment-", "");
    }

}
