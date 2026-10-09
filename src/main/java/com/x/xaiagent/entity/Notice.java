package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.x.xaiagent.constant.NoticeStatus;
import com.x.xaiagent.constant.NoticeType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告/系统通知主表。
 */
@Data
@TableName("notice")
public class Notice {

    /** 主键，UUID 字符串（Java 端生成，IdType.INPUT） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 类型：SYSTEM / ACTIVITY / UPDATE */
    private NoticeType type;

    /** 标题 */
    private String title;

    /** 正文 */
    private String content;

    /** 可见角色集合：逗号分隔，取值 ADMIN / USER / GUEST 的任意组合（如 "USER,GUEST"） */
    private String scope;

    /** 状态：DRAFT / PUBLISHED / WITHDRAWN */
    private NoticeStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 首次发布时间（重复发布不覆盖） */
    private LocalDateTime publishTime;

    /** 最后操作人用户 ID（sys_user.id） */
    private String operatorId;

    /** 逻辑删除：0 未删，1 已删 */
    @TableLogic
    private Integer deleted;
}
