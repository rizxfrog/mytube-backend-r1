package com.mytube.upload.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;

@Component
public class S3Utils {
    @Autowired
    private S3Client s3Client;
    @Autowired
    private S3Presigner s3Presigner;

    @Value("${s3.bucket}")
    private String bucket;

    public void createBucket(String bucket) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ignored) {}
    }

    public void deleteBucket(String bucket) {
        s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucket).build());
    }

    /**
     * 上传文件 上传小文件
     * @param file
     * @param key
     * @return
     */
    public String upload(MultipartFile file, String key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return key;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }


    /**
     * 分片上传 上传大文件
     * @param file 文件
     * @param key 文件名
     * @return 文件名
     */
    public String uploadMultipart(MultipartFile file, String key) {
        try {
            // 1. 创建 multipart upload
            CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(
                    CreateMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build()
            );
            String uploadId = createResponse.uploadId();

            List<CompletedPart> parts = new ArrayList<>();
            byte[] buffer = new byte[5 * 1024 * 1024]; // 5MB 分片
            InputStream inputStream = file.getInputStream();

            int bytesRead;
            int partNumber = 1;

            while ((bytesRead = inputStream.read(buffer)) > 0) {
                UploadPartResponse uploadPartResponse = s3Client.uploadPart(
                        UploadPartRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .uploadId(uploadId)
                                .partNumber(partNumber)
                                .build(),
                        RequestBody.fromBytes(Arrays.copyOf(buffer, bytesRead))
                );

                parts.add(CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(uploadPartResponse.eTag())
                        .build());

                partNumber++;
            }

            // 3. 完成上传
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                    .parts(parts)
                    .build();

            s3Client.completeMultipartUpload(
                    CompleteMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .uploadId(uploadId)
                            .multipartUpload(completedMultipartUpload)
                            .build()
            );

            return key;

        } catch (Exception e) {
            throw new RuntimeException("Multipart Upload Failed", e);
        }
    }


    public void putFile(String bucket, String key, Path path) {
        s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), path);
    }

    public void putObject(String bucket, String key, org.springframework.web.multipart.MultipartFile file) {
        try {
            s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getFile(String bucket, String key, Path path) {
        s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(), path);
    }

    public List<S3Object> listObjects(String bucket) {
        ListObjectsV2Response resp = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).build());
        return resp.contents();
    }

    public void deleteObject(String bucket, String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    /* ===========================
     *  预签名 URL
     * =========================== */

    /** 生成下载 URL */

    public URL presignGet(String bucket, String key, Duration duration) {
        PresignedGetObjectRequest req = s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                .signatureDuration(duration)
                .build());
        return req.url();
    }

    public URL presignPut(String bucket, String key, Duration duration) {
        PresignedPutObjectRequest req = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(key).build())
                .signatureDuration(duration)
                .build());
        return req.url();
    }

    public String createMultipartUpload(String bucket, String key) {
        CreateMultipartUploadResponse resp = s3Client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        return resp.uploadId();
    }

    public CompletedMultipartUpload uploadParts(String bucket, String key, String uploadId, List<Path> partPaths) {
        List<CompletedPart> parts = new ArrayList<>();
        for (int i = 0; i < partPaths.size(); i++) {
            int partNumber = i + 1;
            UploadPartResponse res = s3Client.uploadPart(UploadPartRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .uploadId(uploadId)
                            .partNumber(partNumber)
                            .build(),
                    partPaths.get(i));
            parts.add(CompletedPart.builder().partNumber(partNumber).eTag(res.eTag()).build());
        }
        return CompletedMultipartUpload.builder().parts(parts).build();
    }

    public void completeMultipartUpload(String bucket, String key, String uploadId, CompletedMultipartUpload completed) {
        s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(completed)
                .build());
    }

    public void abortMultipartUpload(String bucket, String key, String uploadId) {
        AbortMultipartUploadRequest req = AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .build();
        s3Client.abortMultipartUpload(req);
    }
}
