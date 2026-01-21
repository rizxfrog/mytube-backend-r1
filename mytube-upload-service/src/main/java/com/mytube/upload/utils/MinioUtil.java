package com.mytube.upload.utils;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MinioUtil {
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;


    /** 上传文件 */
    public String uploadVideo(String objectName, InputStream stream) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object("video" + objectName)
                        .stream(stream, -1, 10485760)
                        .contentType("video/mp4")
                        .build()
        );

    }

    public String uploadImage(MultipartFile file, String folder) {
        try {
            String object = folder + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket("videos")
                        .object(object)
                        .stream(is, is.available(), -1)
                        .contentType("image/jpeg")
                        .build());
            }
            return "/covers/" + object;
        } catch (Exception e) {
            return null;
        }
    }

    public String uploadVideoFile(java.io.File file, String hash) {
        try {
            String object = "video/" + System.currentTimeMillis() + hash + ".mp4";
            try (java.io.InputStream is = new java.io.FileInputStream(file)) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket("videos")
                        .object(object)
                        .stream(is, file.length(), -1)
                        .contentType("video/mp4")
                        .build());
            }
            return "/videos/" + object;
        } catch (Exception e) {
            return null;
        }
    }

    public String presignPut(String bucket, String object, long ttlMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(object)
                            .expiry((int) TimeUnit.MINUTES.toSeconds(ttlMinutes))
                            .build()
            );
        } catch (Exception e) {
            return null;
        }
    }

    public int countByPrefix(String bucket, String prefix) {
        try {
            int count = 0;
            Iterable<Result<Item>> results = minioClient.listObjects(io.minio.ListObjectsArgs.builder().bucket(bucket).prefix(prefix).build());
            for (Result<Item> r : results) { r.get(); count++; }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    public void deleteByPrefix(String bucket, String prefix) {
        try {
            List<io.minio.messages.DeleteObject> objects = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(io.minio.ListObjectsArgs.builder().bucket(bucket).prefix(prefix).recursive(true).build());
            for (Result<Item> r : results) { Item it = r.get(); objects.add(new io.minio.messages.DeleteObject(it.objectName())); }
            if (!objects.isEmpty()) {
                minioClient.removeObjects(io.minio.RemoveObjectsArgs.builder().bucket(bucket).objects(objects).build());
            }
        } catch (Exception ignored) {}
    }

    public String composeVideo(String bucket, String targetObject, List<String> sourceObjects) {
        try {
            List<io.minio.ComposeSource> sources = new ArrayList<>();
            for (String o : sourceObjects) {
                sources.add(io.minio.ComposeSource.builder().bucket(bucket).object(o).build());
            }
            minioClient.composeObject(ComposeObjectArgs.builder().bucket(bucket).object(targetObject).sources(sources).build());
            return "/" + bucket + "/" + targetObject;
        } catch (Exception e) {
            return null;
        }
    }

    /** 创建桶 */
    public void createBucket() throws Exception {
        boolean exist = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build()
        );
        if (!exist) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
