package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.x.xaiagent.entity.ChatMessage;
import com.x.xaiagent.entity.ChatMessageCitation;
import com.x.xaiagent.entity.ConversationVO;
import com.x.xaiagent.mapper.ChatMessageMapper;
import com.x.xaiagent.service.ChatMessageService;
import com.x.xaiagent.service.ChatMessageCitationService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    @Resource
    private ChatMessageCitationService chatMessageCitationService;

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        if (message.getConversationId() == null || message.getConversationId().isBlank()) {
            message.setConversationId(UUID.randomUUID().toString());
        }
        if (message.getUserId() == null) {
            message.setUserId(0L);
        }
        this.save(message);
        return message;
    }

    @Override
    public List<ChatMessage> listByConversation(String conversationId) {
        return this.list(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreateTime));
    }

    @Override
    public List<ConversationVO> listConversations(Long userId) {
        if (userId == null) {
            userId = 0L;
        }
        return baseMapper.listConversations(userId);
    }

    @Override
    public long countByConversation(String conversationId) {
        Long count = baseMapper.countByConversation(conversationId);
        return count == null ? 0L : count;
    }

    @Override
    public List<ChatMessage> listLatestMessages(String conversationId, int limit) {
        return baseMapper.listLatestMessages(conversationId, limit);
    }

    @Override
    public ChatMessage getLastMessage(String conversationId) {
        return baseMapper.selectLastMessage(conversationId);
    }

    @Override
    public List<ChatMessage> searchMessages(String conversationId, String keyword) {
        return baseMapper.searchMessages(conversationId, keyword);
    }

    @Override
    public boolean removeConversation(String conversationId) {
        return this.remove(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCitations(Long messageId, List<Document> citations) {
        if (messageId == null || citations == null || citations.isEmpty()) {
            return;
        }
        List<ChatMessageCitation> list = citations.stream().map(doc -> {
            ChatMessageCitation c = new ChatMessageCitation();
            c.setMessageId(messageId);
            Object name = doc.getMetadata().get("source");
            if (name == null) {
                name = doc.getMetadata().get("filename");
            }
            c.setDocName(name == null ? "未知来源" : String.valueOf(name));
            c.setChunkContent(doc.getText());
            c.setScore(doc.getScore());
            c.setCreateTime(LocalDateTime.now());
            return c;
        }).collect(Collectors.toList());
        chatMessageCitationService.saveBatch(list);
    }
}
