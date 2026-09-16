package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.xaiagent.entity.ChatMessage;
import com.x.xaiagent.entity.ConversationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 按用户聚合出会话列表（逻辑删除的消息不计入），SQL 见 ChatMessageMapper.xml
     */
    List<ConversationVO> listConversations(@Param("userId") Long userId);

    /**
     * 统计某会话消息数（逻辑删除不计入）
     */
    Long countByConversation(@Param("conversationId") String conversationId);

    /**
     * 取某会话最新 N 条消息，按时间正序返回
     */
    List<ChatMessage> listLatestMessages(@Param("conversationId") String conversationId, @Param("limit") int limit);

    /**
     * 取某会话最后一条消息
     */
    ChatMessage selectLastMessage(@Param("conversationId") String conversationId);

    /**
     * 在某会话内按关键词搜索消息（逻辑删除不计入）
     */
    List<ChatMessage> searchMessages(@Param("conversationId") String conversationId, @Param("keyword") String keyword);
}
