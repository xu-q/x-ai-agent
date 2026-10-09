package com.x.xaiagent.constant;

/**
 * 公告类型（入库存 name()，JSON 序列化也是 name()）。
 */
public enum NoticeType {

    /** 系统通知 */
    SYSTEM("系统"),

    /** 活动通知 */
    ACTIVITY("活动"),

    /** 更新通知 */
    UPDATE("更新");

    private final String label;

    NoticeType(String label) {
        this.label = label;
    }

    /** 中文展示名（仅用于日志/文档，前端自行映射文案） */
    public String getLabel() {
        return label;
    }
}
