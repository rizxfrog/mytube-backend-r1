package com.mytube.upload.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PresignService {
    @Autowired
    private MinioClient minioClient;

    public String presignPut(String bucket, String object, long ttlMinutes) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucket)
                        .object(object)
                        .expiry((int) TimeUnit.MINUTES.toSeconds(ttlMinutes))
                        .build()
        );
    }
}
