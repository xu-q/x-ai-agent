package com.x.xaiagent.controller;

import com.x.xaiagent.entity.ChatMessage;
import com.x.xaiagent.entity.ChatMessageCitation;
import com.x.xaiagent.entity.ConversationVO;
import com.x.xaiagent.service.ChatMessageService;
import com.x.xaiagent.service.ChatMessageCitationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatMessageCitationService chatMessageCitationService;

    /**
     * 保存一条消息；不传 conversationId 时自动开新会话并返回
     */
    @PostMapping("/message")
    public ChatMessage saveMessage(@RequestBody ChatMessage message) {
        return chatMessageService.saveMessage(message);
    }

    /**
     * 按会话拉取消息流
     */
    @GetMapping("/message")
    public List<ChatMessage> listMessages(@RequestParam String conversationId) {
        return chatMessageService.listByConversation(conversationId);
    }

    /**
     * 查询会话列表
     */
    @GetMapping("/conversations")
    public List<ConversationVO> listConversations(@RequestParam(defaultValue = "0") Long userId) {
        return chatMessageService.listConversations(userId);
    }

    /**
     * 统计某会话消息数
     */
    @GetMapping("/message/count")
    public long countMessages(@RequestParam String conversationId) {
        return chatMessageService.countByConversation(conversationId);
    }

    /**
     * 取某会话最新 N 条消息
     */
    @GetMapping("/message/latest")
    public List<ChatMessage> latestMessages(@RequestParam String conversationId,
                                             @RequestParam(defaultValue = "20") int limit) {
        return chatMessageService.listLatestMessages(conversationId, limit);
    }

    /**
     * 在某会话内按关键词搜索消息
     */
    @GetMapping("/message/search")
    public List<ChatMessage> searchMessages(@RequestParam String conversationId,
                                             @RequestParam String keyword) {
        return chatMessageService.searchMessages(conversationId, keyword);
    }

    /**
     * 查询某条助手消息引用的知识片段（RAG 引用）
     */
    @GetMapping("/message/citations")
    public List<ChatMessageCitation> listCitations(@RequestParam Long messageId) {
        return chatMessageCitationService.lambdaQuery()
                .eq(ChatMessageCitation::getMessageId, messageId)
                .list();
    }

    /**
     * 删除整个会话（逻辑删除）
     */
    @DeleteMapping("/message")
    public boolean removeConversation(@RequestParam String conversationId) {
        return chatMessageService.removeConversation(conversationId);
    }
}
