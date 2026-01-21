package com.mytube.common.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

public interface ObjectStorageClient {
    String putObject(String key, MultipartFile file);

    String presignPut(String key, Duration ttl);

    String presignGet(String key, Duration ttl);

    int countByPrefix(String prefix);

    void deleteByPrefix(String prefix);

    String composeObject(String targetKey, List<String> sourceKeys);
}
