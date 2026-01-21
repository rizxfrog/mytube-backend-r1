package com.mytube.user.service;

import com.mytube.common.web.CustomResponse;

public interface UserAccountService {
    /**
     * 鐢ㄦ埛娉ㄥ唽
     * @param username 璐﹀彿
     * @param password 瀵嗙爜
     * @return CustomResponse瀵硅薄
     */
    CustomResponse<Void>  register(String username, String password);

    /**
     * 鐢ㄦ埛鐧诲綍
     * @param username 璐﹀彿
     * @param password 瀵嗙爜
     * @return CustomResponse瀵硅薄
     */
    CustomResponse login(String username, String password);

    /**
     * 绠＄悊鍛樼櫥褰?
     * @param username 璐﹀彿
     * @param password 瀵嗙爜
     * @return CustomResponse瀵硅薄
     */
    CustomResponse adminLogin(String username, String password);

    /**
     * 鑾峰彇鐢ㄦ埛涓汉淇℃伅
     * @return CustomResponse瀵硅薄
     */
    CustomResponse personalInfo();

    /**
     * 鑾峰彇绠＄悊鍛樹釜浜轰俊鎭?
     * @return CustomResponse瀵硅薄
     */
    CustomResponse adminPersonalInfo();

    /**
     * 閫€鍑虹櫥褰曪紝娓呯┖redis涓浉鍏崇敤鎴风櫥褰曡璇?
     */
    void logout();

    /**
     * 绠＄悊鍛橀€€鍑虹櫥褰曪紝娓呯┖redis涓浉鍏崇鐞嗗憳鐧诲綍璁よ瘉
     */
    void adminLogout();

    /**
     * 閲嶇疆瀵嗙爜
     * @param pw    鏃у瘑鐮?
     * @param npw   鏂板瘑鐮?
     * @return  鍝嶅簲瀵硅薄
     */
    CustomResponse updatePassword(String pw, String npw);
}
