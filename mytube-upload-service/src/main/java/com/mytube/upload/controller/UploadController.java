package com.mytube.upload.controller;

import com.mytube.common.web.CustomResponse;
import com.mytube.common.po.VideoMeta;
import com.mytube.common.security.CurrentUser;
import com.mytube.upload.pojo.dto.VideoUploadInfoDTO;
import com.mytube.upload.service.video.VideoUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/video")
public class UploadController {
    @Autowired
    private VideoUploadService videoUploadService;
    @Autowired
    private CurrentUser currentUser;

    @GetMapping("/ask-chunk")
    public CustomResponse askChunk(@RequestParam("hash") String hash) {
        return videoUploadService.askCurrentChunk(hash);
    }

    @PostMapping("/upload-chunk")
    public CustomResponse uploadChunk(@RequestParam("hash") String hash,
                                      @RequestParam("index") Integer index) throws IOException {
        return videoUploadService.uploadChunk(null, hash, index);
    }

    @GetMapping("/cancel-upload")
    public CustomResponse cancelUpload(@RequestParam("hash") String hash) {
        return videoUploadService.cancelUpload(hash);
    }

    @PostMapping("/add")
    public CustomResponse add(@RequestParam("cover") MultipartFile cover,
                              @RequestParam("hash") String hash,
                              @RequestParam("title") String title,
                              @RequestParam("type") Long type,
                              @RequestParam("auth") Long auth,
                              @RequestParam("duration") Double duration,
                              @RequestParam("mcid") String mcid,
                              @RequestParam("scid") String scid,
                              @RequestParam("tags") String tags,
                              @RequestParam("descr") String descr) throws IOException {
        VideoUploadInfoDTO dto = new VideoUploadInfoDTO(null, hash, title, type, auth, duration, mcid, scid, tags, descr, null);
        return videoUploadService.addVideo(cover, dto);
    }

    @PostMapping("/direct-upload")
    public CustomResponse<String> directUpload(@RequestParam("video") MultipartFile video,
                                               @RequestParam("cover") MultipartFile cover,
                                               @RequestParam("title") String title,
                                               @RequestParam("type") Integer type,
                                               @RequestParam("duration") Integer duration,
                                               @RequestParam("mcid") String mcid,
                                               @RequestParam("scid") String scid,
                                               @RequestParam(value = "tags", required = false) String tags,
                                               @RequestParam(value = "description", required = false) String description) throws IOException {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return CustomResponse.error(401, "未登录");
        }
        VideoMeta meta = new VideoMeta();
        meta.setTitle(title);
        meta.setType(type == null ? 1 : type);
        meta.setDuration(duration == null ? 0 : duration);
        meta.setMc_id(mcid);
        meta.setSc_id(scid);
        meta.setTags(tags);
        meta.setDescription(description);
        String vid = videoUploadService.uploadVideo(video, cover, uid, meta);
        return CustomResponse.ok(vid);
    }
}
