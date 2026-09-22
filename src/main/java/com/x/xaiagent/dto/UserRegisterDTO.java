package com.x.xaiagent.dto;

import lombok.Data;

/**
 * 注册 / 新建用户入参
 */
@Data
public class UserRegisterDTO {

    /** 用户名（必填） */
    private String username;

    /** 明文密码（必填） */
    private String password;

    /** 手机号（可选） */
    private String phone;

    /** 头像 URL（可选，更新时传入 OSS 返回的地址） */
    private String avatar;

    /** 角色（可选，缺省 USER） */
    private String role;

    /** 状态（可选，仅更新时使用：1 启用，0 禁用） */
    private Integer status;
}
