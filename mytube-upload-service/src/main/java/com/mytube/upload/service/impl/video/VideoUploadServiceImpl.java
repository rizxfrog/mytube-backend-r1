package com.mytube.upload.service.impl.video;

import com.mytube.common.po.VideoMeta;
import com.mytube.common.po.dao.VideoDAO;
import com.mytube.common.po.dao.VideoStatsDAO;
import com.mytube.common.web.CustomResponse;
import com.mytube.common.redis.RedisUtil;
import com.mytube.common.security.CurrentUser;
import com.mytube.upload.domain.UserVideo;
import com.mytube.upload.mapper.VideoMapper;
import com.mytube.upload.mapper.VideoStatsMapper;
import com.mytube.upload.mapper.UserVideoMapper;
import com.mytube.upload.pojo.dto.VideoUploadInfoDTO;
import com.mytube.upload.service.video.VideoUploadService;
import com.mytube.common.storage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class VideoUploadServiceImpl implements VideoUploadService {
    @Value("${directory.cover:cover}")
    private String COVER_DIRECTORY;
    @Value("${directory.video:video}")
    private String VIDEO_DIRECTORY;
    @Value("${directory.chunk:chunk}")
    private String CHUNK_DIRECTORY;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;
    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired(required = false)
    private RedisUtil redisUtil;

    @Autowired
    private CurrentUser currentUser;

    @Autowired
    private ObjectStorageClient objectStorageClient;

    @Autowired(required = false)
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadVideo(MultipartFile video, MultipartFile cover, Long uid, VideoMeta metainfo) throws IOException {
        if (uid == null || video == null || cover == null || metainfo == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (metainfo.getTitle() == null || metainfo.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        long ts = System.currentTimeMillis();
        String videoKey = "video/" + uid + "/" + ts + ".mp4";
        String coverKey = "cover/" + uid + "/" + ts + ".jpg";
        objectStorageClient.putObject(coverKey, cover);
        objectStorageClient.putObject(videoKey, video);

        Date now = new Date();
        VideoDAO v = new VideoDAO();
        v.setUid(uid);
        v.setTitle(metainfo.getTitle());
        v.setType((long) metainfo.getType());
        v.setAuth(0L);
        v.setDuration((double) metainfo.getDuration());
        v.setMc_id(metainfo.getMc_id());
        v.setSc_id(metainfo.getSc_id());
        v.setTags(metainfo.getTags());
        v.setDescr(metainfo.getDescription());
        v.setCover_url(coverKey);
        v.setVideo_url(videoKey);
        v.setStatus(0);
        v.setUpload_date(now);
        videoMapper.insert(v);
        // 服务层增加兜底查询，如果驱动未回填主键则根据唯一键取回：
        if (v.getVid() == null) {
            VideoDAO latest = videoMapper.selectOne(new QueryWrapper<VideoDAO>().eq("video_url", videoKey).eq("uid", uid));
            if (latest != null) {
                v.setVid(latest.getVid());
            }
        }

        VideoStatsDAO stats = new VideoStatsDAO(v.getVid(),0,0,0,0,0,0,0,0);
        videoStatsMapper.insert(stats);

        UserVideo uv = new UserVideo();
        uv.setUid(uid);
        uv.setVid(v.getVid());
        uv.setPlay(0L);
        uv.setLove(0L);
        uv.setUnlove(0L);
        uv.setCoin(0L);
        uv.setCollect(0L);
        uv.setPlay_time(now);
        userVideoMapper.insert(uv);

        if (redisUtil != null) {
            CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("video:" + v.getVid(), v), taskExecutor != null ? taskExecutor : Runnable::run);
            CompletableFuture.runAsync(() -> redisUtil.addMember("video_status:0", v.getVid()), taskExecutor != null ? taskExecutor : Runnable::run);
            CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("videoStats:" + v.getVid(), stats), taskExecutor != null ? taskExecutor : Runnable::run);
        }

        return String.valueOf(v.getVid());
    }

    @Override
    public CustomResponse askCurrentChunk(String hash) {
        int count = objectStorageClient.countByPrefix("chunk/" + hash + "-");
        return CustomResponse.ok(count);
    }

    @Override
    public CustomResponse uploadChunk(MultipartFile chunk, String hash, Integer index) throws IOException {
        String object = "chunk/" + hash + "-" + index;
        String url = objectStorageClient.presignPut(object, java.time.Duration.ofMinutes(10));
        return CustomResponse.ok(url);
    }

    @Override
    public CustomResponse cancelUpload(String hash) {
        objectStorageClient.deleteByPrefix("chunk/" + hash + "-");
        return new CustomResponse();
    }

    @Override
    public CustomResponse addVideo(MultipartFile cover, VideoUploadInfoDTO dto) throws IOException {
        Long uid = currentUser.requireUserId();
        if (uid == null) {
            return new CustomResponse(401, "未登录", null);
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return new CustomResponse(500, "标题不能为空", null);
        }
        if (dto.getTitle().length() > 80) {
            return new CustomResponse(500, "标题不能超过80字", null);
        }
        if (dto.getDescr() != null && dto.getDescr().length() > 2000) {
            return new CustomResponse(500, "简介太长啦", null);
        }
        String coverKey = "cover/" + uid + "/" + System.currentTimeMillis() + ".jpg";
        String coverUrl = objectStorageClient.putObject(coverKey, cover);
        dto.setCoverUrl(coverUrl);
        dto.setUid(uid);
        CompletableFuture.runAsync(() -> {
            try {
                mergeChunksAndPersist(dto);
            } catch (IOException ignored) {}
        }, taskExecutor != null ? taskExecutor : Runnable::run);
        return new CustomResponse();
    }

    @Override
    public void submitVideo(VideoUploadInfoDTO dto) throws IOException {
        if (dto.getCoverUrl() == null) {
            dto.setCoverUrl("");
        }
        mergeChunksAndPersist(dto);
    }

    @Transactional
    public void mergeChunksAndPersist(VideoUploadInfoDTO vui) throws IOException {
        int count = objectStorageClient.countByPrefix("chunk/" + vui.getHash() + "-");
        if (count == 0) { return; }
        List<String> sources = new ArrayList<>();
        for (int i = 0; i < count; i++) { sources.add("chunk/" + vui.getHash() + "-" + i); }
        long ts = System.currentTimeMillis();
        String target = "video/" + ts + vui.getHash() + ".mp4";
        String videoUrl = objectStorageClient.composeObject(target, sources);
        objectStorageClient.deleteByPrefix("chunk/" + vui.getHash() + "-");
        Date now = new Date();
        VideoDAO video = new VideoDAO();
        video.setUid(vui.getUid());
        video.setTitle(vui.getTitle());
        video.setType(vui.getType());
        video.setAuth(vui.getAuth());
        video.setDuration(vui.getDuration());
        video.setMc_id(vui.getMcId());
        video.setSc_id(vui.getScId());
        video.setTags(vui.getTags());
        video.setDescr(vui.getDescr());
        video.setCover_url(vui.getCoverUrl());
        video.setVideo_url(videoUrl);
        video.setStatus(0);
        video.setUpload_date(now);
        videoMapper.insert(video);
        VideoStatsDAO stats = new VideoStatsDAO(video.getVid(),0,0,0,0,0,0,0,0);
        videoStatsMapper.insert(stats);
        if (redisUtil != null) {
            CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("video:" + video.getVid(), video), taskExecutor != null ? taskExecutor : Runnable::run);
            CompletableFuture.runAsync(() -> redisUtil.addMember("video_status:0", video.getVid()), taskExecutor != null ? taskExecutor : Runnable::run);
            CompletableFuture.runAsync(() -> redisUtil.setExObjectValue("videoStats:" + video.getVid(), stats), taskExecutor != null ? taskExecutor : Runnable::run);
        }
    }
}
