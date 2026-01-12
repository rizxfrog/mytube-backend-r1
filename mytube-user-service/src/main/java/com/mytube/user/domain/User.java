package com.mytube.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Integer uid;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String background;
    private Integer gender; // 鎬у埆锛?濂虫€?1鐢锋€?2鏃犳€у埆锛岄粯璁?
    private String description;
    private Integer exp;    // 缁忛獙鍊?50/200/1500/4500/10800/28800 鍒嗗埆鏄?~6绾х殑鍖洪棿
    private Double coin;    // 纭竵鏁?淇濈暀涓€浣嶅皬鏁?
    private Integer vip;    // 0 鏅€氱敤鎴凤紝1 鏈堝害澶т細鍛橈紝2 瀛ｅ害澶т細鍛橈紝3 骞村害澶т細鍛?
    private Integer state;  // 0 姝ｅ父锛? 灏佺涓紝2 宸叉敞閿€
    private Integer role;   // 0 鏅€氱敤鎴凤紝1 鏅€氱鐞嗗憳锛? 瓒呯骇绠＄悊鍛?
    private Integer auth;   // 0 鏅€氱敤鎴凤紝1 涓汉璁よ瘉锛? 鏈烘瀯璁よ瘉
    private String authMsg; // 璁よ瘉淇℃伅锛屽 teriteri瀹樻柟璐﹀彿
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date createDate;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Shanghai")
    private Date deleteDate;
}

