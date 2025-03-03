package com.example.border.service.impl;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Service
public class MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);
    private final MinioClient minioClient;
    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(MultipartFile file) throws Exception {
        String objectName = sanitizeFileName(file.getOriginalFilename());
        logger.info("Uploading file: {}", objectName);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (MinioException e) {
            logger.error("Failed to upload file: {}", objectName, e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    public byte[] downloadFile(String objectName) throws Exception {
        logger.info("Downloading file: {}", objectName);
        fileNotExists(objectName);
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        )) {
            logger.info("File downloaded successfully: {}", objectName);
            return IOUtils.toByteArray(inputStream);
        } catch (MinioException e) {
            logger.error("Failed to download file: {}", objectName, e);
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    public String deleteFile(String objectName) throws Exception {
        logger.info("Deleting file: {}", objectName);

        try {
            fileNotExists(objectName);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            logger.info("File deleted successfully: {}", objectName);
            return "Object with name " + objectName + " is successfully deleted";
        } catch (MinioException e) {
            logger.error("Failed to delete file: {}", objectName, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }
    }

    private void fileNotExists(String objectName) throws Exception {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (ErrorResponseException e) {
            logger.warn("File not found: {}", objectName);
            throw new FileNotFoundException("Object with name " + objectName + " does not exist");
        }
    }

    private String sanitizeFileName(String fileName) {
        fileName = fileName.replaceAll(" ", "_");

        fileName = fileName.toLowerCase();

        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "");

        return fileName;
    }
}
