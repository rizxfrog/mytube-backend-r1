package com.mytube.user.service;

import com.mytube.common.web.CustomResponse;
import com.mytube.user.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    /**
     * 鏍规嵁uid鏌ヨ鐢ㄦ埛淇℃伅
     * @param id 鐢ㄦ埛ID
     * @return 鐢ㄦ埛鍙淇℃伅瀹炰綋绫?UserDTO
     */
    UserDTO getUserById(Long id);

    /**
     * 鏍规嵁鏈夊簭uid鍒楄〃鏌ヨ鐢ㄦ埛淇℃伅
     * @param list 鐢ㄦ埛id鍒楄〃
     * @return  鐢ㄦ埛淇℃伅鍒楄〃
     */
    List<UserDTO> getUserByIdList(List<Long> list);

    /**
     * 鏇存柊鐢ㄦ埛涓汉淇℃伅
     * @param uid   鐢ㄦ埛uid
     * @param nickname  鏄电О
     * @param desc  涓€х鍚?
     * @param gender    鎬у埆锛?0 濂?1 鐢?2 鍙屾€т汉
     * @return  鍝嶅簲瀵硅薄
     */
    CustomResponse updateUserInfo(Long uid, String nickname, String desc, Integer gender) throws IOException;

    /**
     * 鏇存柊鐢ㄦ埛澶村儚
     * @param uid
     * @param file
     * @return
     */
    CustomResponse updateUserAvatar(Long uid, MultipartFile file) throws IOException;

    CustomResponse updateUserAvatarUrl(Long uid, String avatarUrl);
}

