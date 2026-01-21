package com.mytube.upload.storage;

import com.mytube.common.storage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObject;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartCopyRequest;
import software.amazon.awssdk.services.s3.model.UploadPartCopyResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class S3ObjectStorageClient implements ObjectStorageClient {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3ObjectStorageClient(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${s3.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
    }

    @Override
    public String putObject(String key, MultipartFile file) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload object", e);
        }
    }

    @Override
    public String presignPut(String key, Duration ttl) {
        PresignedPutObjectRequest req = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(key).build())
                        .signatureDuration(ttl)
                        .build()
        );
        return req.url().toString();
    }

    @Override
    public String presignGet(String key, Duration ttl) {
        PresignedGetObjectRequest req = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .getObjectRequest(software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build())
                        .signatureDuration(ttl)
                        .build()
        );
        return req.url().toString();
    }

    @Override
    public int countByPrefix(String prefix) {
        ListObjectsV2Response resp = s3Client.listObjectsV2(
                ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build()
        );
        return resp.contents().size();
    }

    @Override
    public void deleteByPrefix(String prefix) {
        ListObjectsV2Response resp = s3Client.listObjectsV2(
                ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).build()
        );
        if (resp.contents().isEmpty()) {
            return;
        }
        List<DeleteObject> objects = new ArrayList<>();
        for (S3Object obj : resp.contents()) {
            objects.add(DeleteObject.builder().key(obj.key()).build());
        }
        s3Client.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(objects).build())
                .build());
    }

    @Override
    public String composeObject(String targetKey, List<String> sourceKeys) {
        if (sourceKeys == null || sourceKeys.isEmpty()) {
            return null;
        }
        CreateMultipartUploadResponse create = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(targetKey)
                        .build()
        );
        String uploadId = create.uploadId();
        List<CompletedPart> parts = new ArrayList<>();
        try {
            for (int i = 0; i < sourceKeys.size(); i++) {
                int partNumber = i + 1;
                String copySource = bucket + "/" + sourceKeys.get(i);
                UploadPartCopyResponse part = s3Client.uploadPartCopy(
                        UploadPartCopyRequest.builder()
                                .bucket(bucket)
                                .key(targetKey)
                                .uploadId(uploadId)
                                .partNumber(partNumber)
                                .copySource(copySource)
                                .build()
                );
                parts.add(CompletedPart.builder().partNumber(partNumber).eTag(part.copyPartResult().eTag()).build());
            }
            CompleteMultipartUploadResponse complete = s3Client.completeMultipartUpload(
                    CompleteMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(targetKey)
                            .uploadId(uploadId)
                            .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build())
                            .build()
            );
            return complete.location() == null ? targetKey : targetKey;
        } catch (Exception e) {
            s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(targetKey)
                    .uploadId(uploadId)
                    .build());
            throw new RuntimeException("Failed to compose object", e);
        }
    }
}
