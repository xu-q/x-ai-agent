package com.x.xaiagent.vo;

import com.x.xaiagent.constant.NoticeStatus;
import com.x.xaiagent.constant.NoticeType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端公告视图（GET /notices 列表项 / 新建、编辑返回）。
 */
@Data
public class NoticeVO {

    private String id;

    private NoticeType type;

    private String title;

    private String content;

    /** 可见角色集合（ADMIN / USER / GUEST 任意组合） */
    private List<String> scopes;

    private NoticeStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime publishTime;
}
