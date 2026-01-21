package com.mytube.upload.service.video;

import com.mytube.common.po.VideoMeta;
import com.mytube.common.web.CustomResponse;
import com.mytube.upload.pojo.dto.VideoUploadInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoUploadService {
    String uploadVideo(MultipartFile video, MultipartFile cover, Long uid, VideoMeta metainfo) throws IOException;

    CustomResponse askCurrentChunk(String hash);
    CustomResponse uploadChunk(MultipartFile chunk, String hash, Integer index) throws IOException;
    CustomResponse cancelUpload(String hash);
    CustomResponse addVideo(MultipartFile cover, VideoUploadInfoDTO videoUploadInfoDTO) throws IOException;
    void submitVideo(VideoUploadInfoDTO videoUploadInfoDTO) throws IOException;
}
