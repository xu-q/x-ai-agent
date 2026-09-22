package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class User {

    /** 主键，UUID 字符串（Java 端生成，IdType.INPUT） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 登录用户名，唯一 */
    private String username;

    /** BCrypt 加密后的密码 */
    private String password;

    /** 手机号（可选） */
    private String phone;

    /** 头像 URL（可选，OSS 直链或签名 URL） */
    private String avatar;

    /** 角色：ADMIN / USER / GUEST */
    private String role;

    /** 状态：1 启用，0 禁用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 逻辑删除：0 未删，1 已删 */
    @TableLogic
    private Integer deleted;
}
