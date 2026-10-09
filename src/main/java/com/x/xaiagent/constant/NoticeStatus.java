package com.x.xaiagent.constant;

/**
 * 公告状态。
 */
public enum NoticeStatus {

    /** 草稿（新建默认，未发布） */
    DRAFT("草稿"),

    /** 已发布（用户端可见） */
    PUBLISHED("已发布"),

    /** 已撤回（用户端不可见） */
    WITHDRAWN("已撤回");

    private final String label;

    NoticeStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
