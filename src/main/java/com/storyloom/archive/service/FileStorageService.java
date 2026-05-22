package com.storyloom.archive.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.name}")
    private String bucketName;

    public FileStorageService(
            @Value("${minio.url}") String minioUrl,
            @Value("${minio.access.key}") String accessKey,
            @Value("${minio.secret.key}") String secretKey) {
        
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String newFileName = UUID.randomUUID().toString() + extension;

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(newFileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            return newFileName; 
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file in MinIO Cloud Storage", e);
        }
    }
}