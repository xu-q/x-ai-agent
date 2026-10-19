package com.x.xaiagent.vo;

import lombok.Data;

/**
 * 重置密码结果：返回实际生效的新密码（尤其是使用默认重置时），便于管理员告知用户。
 */
@Data
public class ResetPasswordVO {

    private String id;

    private String username;

    /** 实际生效的新密码明文 */
    private String newPassword;
}
