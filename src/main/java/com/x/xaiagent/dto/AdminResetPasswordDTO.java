package com.x.xaiagent.dto;

import lombok.Data;

/**
 * 管理员重置密码请求：可指定新密码；留空则重置为默认密码。
 */
@Data
public class AdminResetPasswordDTO {

    /** 管理员指定的新密码；为空则使用系统默认重置密码 */
    private String newPassword;
}
