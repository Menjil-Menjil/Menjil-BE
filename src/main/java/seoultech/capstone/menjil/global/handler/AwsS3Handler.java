package seoultech.capstone.menjil.global.handler;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Duration;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class AwsS3Handler {

    private final AmazonS3Client amazonS3Client;

    public URL generatePresignedUrl(String bucketName, String objectKey, Duration duration) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(com.amazonaws.HttpMethod.GET);

        // Set expiration
        Date expiration = new Date();
        expiration.setTime(System.currentTimeMillis() + duration.toMillis());
        generatePresignedUrlRequest.setExpiration(expiration);

        return amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }

}
