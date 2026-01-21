package com.mytube.upload.provider;

import com.mytube.api.upload.UploadServiceApi;
import com.mytube.common.storage.ObjectStorageClient;
import com.mytube.upload.pojo.dto.VideoUploadInfoDTO;
import com.mytube.upload.service.video.VideoUploadService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class UploadServiceProvider implements UploadServiceApi {
    @Autowired
    private ObjectStorageClient objectStorageClient;
    @Autowired
    private VideoUploadService videoUploadService;

    @Override
    public Integer askChunk(String hash) {
        return objectStorageClient.countByPrefix("chunk/" + hash + "-");
    }

    @Override
    public String uploadChunk(String hash, Integer index, byte[] bytes) {
        String object = "chunk/" + hash + "-" + index;
        return objectStorageClient.presignPut(object, java.time.Duration.ofMinutes(10));
    }

    @Override
    public String cancelUpload(String hash) {
        objectStorageClient.deleteByPrefix("chunk/" + hash + "-");
        return "ok";
    }

    @Override
    public String submitVideo(String hash, String title, String type, Integer auth, Integer duration, Integer mcid, Integer scid, String tags, String descr) {
        VideoUploadInfoDTO dto = new VideoUploadInfoDTO();
        dto.setHash(hash);
        dto.setTitle(title);
        dto.setType(type == null ? 0L : Long.parseLong(type));
        dto.setAuth(auth == null ? 0L : auth.longValue());
        dto.setDuration(duration == null ? 0.0 : duration.doubleValue());
        dto.setMcId(mcid == null ? null : String.valueOf(mcid));
        dto.setScId(scid == null ? null : String.valueOf(scid));
        dto.setTags(tags);
        dto.setDescr(descr);
        try {
            videoUploadService.submitVideo(dto);
            return "ok";
        } catch (Exception e) {
            return "failed";
        }
    }
}
