package com.x.xaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.x.xaiagent.entity.ChatMessage;
import com.x.xaiagent.entity.ConversationVO;
import org.springframework.ai.document.Document;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 保存一条消息；若 conversationId 为空则自动生成新会话ID
     */
    ChatMessage saveMessage(ChatMessage message);

    /**
     * 按会话拉取消息（按时间正序，便于还原对话流）
     */
    List<ChatMessage> listByConversation(String conversationId);

    /**
     * 查询某用户的会话列表
     */
    List<ConversationVO> listConversations(Long userId);

    /**
     * 统计某会话消息数
     */
    long countByConversation(String conversationId);

    /**
     * 取某会话最新 N 条消息（按时间正序），用于上下文截取 / 预览
     */
    List<ChatMessage> listLatestMessages(String conversationId, int limit);

    /**
     * 取某会话最后一条消息
     */
    ChatMessage getLastMessage(String conversationId);

    /**
     * 在某会话内按关键词搜索消息
     */
    List<ChatMessage> searchMessages(String conversationId, String keyword);

    /**
     * 逻辑删除整个会话的消息
     */
    boolean removeConversation(String conversationId);

    /**
     * 保存助手消息引用的知识片段（RAG 引用落库）
     *
     * @param messageId 助手消息ID
     * @param citations 本次回答检索到的知识文档
     */
    void saveCitations(Long messageId, List<Document> citations);
}
