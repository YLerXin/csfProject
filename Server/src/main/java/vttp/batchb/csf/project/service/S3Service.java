package vttp.batchb.csf.project.service;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

@Service
public class S3Service{
    @Autowired
    private AmazonS3 s3Client;

    @Value("${do.storage.bucket}")
    private String bucketName;

    @Value("${do.storage.endpoint}")
    private String endPoint;

    public String uploadProfilePicture(MultipartFile file, String userId) throws IOException {
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        
        Map<String, String> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("fileName", file.getOriginalFilename());
        userData.put("uploadDateTime", LocalDateTime.now().toString());
        metadata.setUserMetadata(userData);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unknown.png";
        }
        String extension = "";
        int dotPos = originalFilename.lastIndexOf('.');
        if (dotPos >= 0) {
            extension = originalFilename.substring(dotPos + 1);
        }
        if (extension.isBlank()) {
            extension = "png";
        }

        String key = "profile-pictures/%s.%s".formatted(userId, extension);

        PutObjectRequest req = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)
                                    .withCannedAcl(CannedAccessControlList.PublicRead);

        PutObjectResult result = s3Client.putObject(req);

        return "https://%s.%s/%s".formatted(bucketName, endPoint, key);
    }
    public void deleteFile(String objectKey) {
        s3Client.deleteObject(bucketName, objectKey);
    }

    public String uploadProductImage(MultipartFile file, String productId) throws IOException {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        Map<String, String> userData = new HashMap<>();
        userData.put("productId", productId);
        userData.put("fileName", file.getOriginalFilename());
        userData.put("uploadDateTime", LocalDateTime.now().toString());
        metadata.setUserMetadata(userData);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unknown.png";
        }
        String extension = "";
        int dotPos = originalFilename.lastIndexOf('.');
        if (dotPos >= 0) {
            extension = originalFilename.substring(dotPos + 1);
        }
        if (extension.isBlank()) {
            extension = "png";
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String key = "product-images/%s-%s.%s".formatted(productId, timestamp, extension);

        PutObjectRequest req = new PutObjectRequest(
            bucketName,
            key,
            file.getInputStream(),
            metadata
        ).withCannedAcl(CannedAccessControlList.PublicRead);

        PutObjectResult result = s3Client.putObject(req);

        return "https://%s.%s/%s".formatted(bucketName, endPoint, key);
    }
    
}
