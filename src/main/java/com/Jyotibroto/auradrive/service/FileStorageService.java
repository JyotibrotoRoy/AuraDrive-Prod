package com.Jyotibroto.auradrive.service;

import io.awspring.cloud.s3.S3Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    private final S3Template s3Template;

    @Value("${s3.bucket.name}")
    private String bucketName;

    public FileStorageService(S3Template s3Template) {
        this.s3Template = s3Template;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        s3Template.upload(bucketName, fileName, file.getInputStream());

        return "https://%s.s3.ap-south-1.amazonaws.com/%s".formatted(bucketName, fileName);
    }

    public void deleteFile(String fileUrl) {
        try{
            URL url = new URL(fileUrl);
            String key = url.getPath().substring(1);

            s3Template.deleteObject(bucketName, key);

        }catch (Exception e) {
            log.error("Error deleting file from S3: {}", fileUrl, e);
        }
    }
}
