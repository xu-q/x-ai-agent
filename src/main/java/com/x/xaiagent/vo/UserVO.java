package com.x.xaiagent.vo;

import com.x.xaiagent.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象（剔除密码等敏感字段）
 */
@Data
public class UserVO {

    private String id;
    private String username;
    private String phone;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static UserVO from(User u) {
        if (u == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setPhone(u.getPhone());
        vo.setRole(u.getRole());
        vo.setStatus(u.getStatus());
        vo.setCreateTime(u.getCreateTime());
        vo.setUpdateTime(u.getUpdateTime());
        return vo;
    }
}
