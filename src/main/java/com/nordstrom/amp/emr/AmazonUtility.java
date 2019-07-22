package com.nordstrom.amp.emr;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class AmazonUtility {
    private final static Logger log = LoggerFactory.getLogger(AmazonUtility.class);

    @Autowired
    private AmazonS3 s3Client;

    public String appendS3File(String bucket, String fileKey, String content){
        String toWriteToS3 = "";

        try {
            toWriteToS3 = getS3Object(bucket, fileKey);
        } catch (AmazonS3Exception ex) {
            log.error(String.format("AmazonS3Exception: [{}]", fileKey), ex);
        } catch (IOException ex) {
            log.error(String.format("IOException [{}]", fileKey), ex);

        }

        toWriteToS3 += (toWriteToS3.equals("")) ? "" : "\n";
        toWriteToS3 += content;

        s3Client.putObject(bucket, fileKey, toWriteToS3);
        return toWriteToS3;
    }

    public String getS3ObjectHelper(String bucket, String fileKey) {

        return ExponentialBackoff.execute(5, () -> getS3Object(bucket, fileKey));
    }

    private String getS3Object(String bucket, String fileKey) throws IOException {
        try {
            S3Object s3Object = s3Client.getObject(bucket, fileKey);

            InputStream inputStream = s3Object.getObjectContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String content = "";
            while (true) {
                String line = reader.readLine();

                if (line == null) {
                    break;
                }

                content += line;
            }

            reader.close();

            return content;
        } catch (IOException ex) {
            log.error("Unable to getS3Object", ex);
            throw ex;
        }
    }
}
