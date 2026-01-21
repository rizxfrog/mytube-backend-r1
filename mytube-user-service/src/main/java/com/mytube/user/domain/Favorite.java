package com.mytube.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Integer fid;    // 鏀惰棌澶笽D
    private Integer uid;    // 鎵€灞炵敤鎴稩D
    private Integer type;   // 鏀惰棌澶圭被鍨?1榛樿鏀惰棌澶?2鐢ㄦ埛鍒涘缓
    private Integer visible;    // 瀵瑰寮€鏀?0闅愯棌 1鍏紑
    private String cover;   // 鏀惰棌澶瑰皝闈rl
    private String title;   // 鏀惰棌澶瑰悕绉?
    private String description; // 绠€浠?
    private Integer count;  // 鏀惰棌澶逛腑瑙嗛鏁伴噺
    private Integer isDelete;   // 鏄惁鍒犻櫎 1宸插垹闄?
}

