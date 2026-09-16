package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话消息引用的知识片段（RAG 引用落库，存快照）
 */
@Data
@TableName("chat_message_citation")
public class ChatMessageCitation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联助手消息ID（chat_message.id） */
    private Long messageId;

    /** 来源文档名 */
    private String docName;

    /** 命中的知识片段内容 */
    private String chunkContent;

    /** 相似度分数 */
    private Double score;

    /** 创建时间 */
    private LocalDateTime createTime;
}
