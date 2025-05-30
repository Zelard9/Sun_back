package org.example.sun_back.service.AWS3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String uploadFile(File file) {
        try {
            String fileName = "uploads/" + System.currentTimeMillis() + "_" + file.getName();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());

            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file)
                    .withMetadata(metadata); // ❌ без .withCannedAcl(...)

            amazonS3.putObject(request);

            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Помилка завантаження файлу на S3", e);
        }
    }


    public void deleteFileFromUrl(String url) {
        String key = url.substring(url.indexOf("uploads/"));
        amazonS3.deleteObject(bucketName, key);
    }

    public String getFileUrl(String fileName) {
        return amazonS3.getUrl(bucketName, fileName).toString();
    }
}
