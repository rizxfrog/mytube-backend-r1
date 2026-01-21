package com.mytube.upload.utils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
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
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class S3StaticUtils {
    public static S3Client newS3Client(String endpoint, String accessKey, String secretKey) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    public static S3Presigner newPresigner(String endpoint, String accessKey, String secretKey) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    public static void createBucket(S3Client s3, String bucket) {
        try {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ignored) {}
    }

    public static void deleteBucket(S3Client s3, String bucket) {
        s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucket).build());
    }

    public static void putFile(S3Client s3, String bucket, String key, Path path) {
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), path);
    }

    public static void getFile(S3Client s3, String bucket, String key, Path path) {
        s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(), path);
    }

    public static List<S3Object> listObjects(S3Client s3, String bucket) {
        ListObjectsV2Response resp = s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).build());
        return resp.contents();
    }

    public static void deleteObject(S3Client s3, String bucket, String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    public static URL presignGet(S3Presigner presigner, String bucket, String key, Duration duration) {
        PresignedGetObjectRequest req = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                .signatureDuration(duration)
                .build());
        return req.url();
    }

    public static URL presignPut(S3Presigner presigner, String bucket, String key, Duration duration) {
        PresignedPutObjectRequest req = presigner.presignPutObject(PutObjectPresignRequest.builder()
                .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(key).build())
                .signatureDuration(duration)
                .build());
        return req.url();
    }

    public static String createMultipartUpload(S3Client s3, String bucket, String key) {
        CreateMultipartUploadResponse resp = s3.createMultipartUpload(CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        return resp.uploadId();
    }

    public static CompletedMultipartUpload uploadParts(S3Client s3, String bucket, String key, String uploadId, List<Path> partPaths) {
        List<CompletedPart> parts = new ArrayList<>();
        for (int i = 0; i < partPaths.size(); i++) {
            int partNumber = i + 1;
            UploadPartResponse res = s3.uploadPart(UploadPartRequest.builder()
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

    public static void completeMultipartUpload(S3Client s3, String bucket, String key, String uploadId, CompletedMultipartUpload completed) {
        s3.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(completed)
                .build());
    }

    public static void abortMultipartUpload(S3Client s3, String bucket, String key, String uploadId) {
        software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest req = software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .build();
        s3.abortMultipartUpload(req);
    }
}
