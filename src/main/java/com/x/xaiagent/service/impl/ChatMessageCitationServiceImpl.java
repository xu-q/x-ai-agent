package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.x.xaiagent.entity.ChatMessageCitation;
import com.x.xaiagent.mapper.ChatMessageCitationMapper;
import com.x.xaiagent.service.ChatMessageCitationService;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageCitationServiceImpl extends ServiceImpl<ChatMessageCitationMapper, ChatMessageCitation>
        implements ChatMessageCitationService {
}
