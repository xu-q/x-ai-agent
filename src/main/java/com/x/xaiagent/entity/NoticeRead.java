package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知已读记录：(user_id, notice_id) 唯一，存在即已读。
 */
@Data
@TableName("notice_read")
public class NoticeRead {

    /** 主键，UUID 字符串（Java 端生成，IdType.INPUT） */
    @TableId(type = IdType.INPUT)
    private String id;

    private String userId;

    private String noticeId;

    private LocalDateTime createTime;
}
