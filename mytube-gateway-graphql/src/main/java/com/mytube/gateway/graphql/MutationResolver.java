package com.mytube.gateway.graphql;

import com.mytube.api.user.UserServiceApi;
import com.mytube.api.upload.UploadServiceApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MutationResolver {
    private static final Logger log = LoggerFactory.getLogger(MutationResolver.class);
    @DubboReference
    private UserServiceApi userServiceApi;
    @DubboReference
    private UploadServiceApi uploadServiceApi;

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @return msg
     */
    @MutationMapping
    public String userRegister(@Argument("username") String username, @Argument("password") String password) {
        log.info("userRegister {}", username);
        if (username == null || password == null) return "参数不能为空";
        return userServiceApi.register(username, password);
    }

    @MutationMapping
    public String userLogin(@Argument("username") String username,
                            @Argument("password") String password) {
        if (username == null || password == null) return "参数不能为空";
        return userServiceApi.login(username, password);
    }
    @MutationMapping
    public String adminLogin(@Argument("username") String username, @Argument("password") String password) {
        if (username == null || password == null) return "参数不能为空";
        return userServiceApi.adminLogin(username, password);
    }

    @MutationMapping
    public String uploadChunkUrl(@Argument("hash") String hash, @Argument("index") Integer index) {
        return uploadServiceApi.uploadChunk(hash, index, null);
    }

    @MutationMapping
    public String uploadCancel(@Argument("hash") String hash) {
        return uploadServiceApi.cancelUpload(hash);
    }
}
