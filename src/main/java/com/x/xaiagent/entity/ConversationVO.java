package com.x.xaiagent.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表视图对象（由 chat_message 按 conversationId 聚合得到）
 */
@Data
public class ConversationVO {

    /** 会话ID */
    private String conversationId;

    /** 首条消息时间 */
    private LocalDateTime startTime;

    /** 最后消息时间 */
    private LocalDateTime lastTime;

    /** 消息总数 */
    private Long messageCount;
}
