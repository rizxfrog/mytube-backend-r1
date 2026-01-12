package com.mytube.api.upload;

public interface UploadServiceApi {
    Integer askChunk(String hash);
    String uploadChunk(String hash, Integer index, byte[] bytes);
    String cancelUpload(String hash);
    String submitVideo(String hash, String title, String type, Integer auth, Integer duration, Integer mcid, Integer scid, String tags, String descr);
}

