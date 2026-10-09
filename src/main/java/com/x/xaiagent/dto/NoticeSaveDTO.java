package com.x.xaiagent.dto;

import com.x.xaiagent.constant.NoticeType;
import lombok.Data;

import java.util.List;

/**
 * 公告新建 / 编辑入参（新建默认状态为草稿，发布走 publish 接口）。
 */
@Data
public class NoticeSaveDTO {

    /** 类型（必填） */
    private NoticeType type;

    /** 标题（必填，≤100 字符） */
    private String title;

    /** 正文（必填） */
    private String content;

    /** 可见角色集合（必填，ADMIN / USER / GUEST 任意组合） */
    private List<String> scopes;
}
