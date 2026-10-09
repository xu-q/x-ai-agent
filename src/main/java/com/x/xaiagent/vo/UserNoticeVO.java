package com.x.xaiagent.vo;

import com.x.xaiagent.constant.NoticeType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端通知视图（GET /user/notices 列表项）。
 */
@Data
public class UserNoticeVO {

    private String id;

    private NoticeType type;

    private String title;

    private String content;

    private LocalDateTime createTime;

    /** 当前用户是否已读 */
    private Boolean read;
}
