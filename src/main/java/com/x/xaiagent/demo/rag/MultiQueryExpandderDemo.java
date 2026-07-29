package com.x.xaiagent.demo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 多查询扩展器示例，输入的查询会进行多查询扩展，返回多个查询
 */
@Component
public class MultiQueryExpandderDemo {

    private final ChatClient.Builder chatClientBuilder;

    public MultiQueryExpandderDemo(ChatModel dashscopeChatModel) {
        this.chatClientBuilder = ChatClient.builder(dashscopeChatModel);
    }

    public List<Query> expand(String query) {
        MultiQueryExpander build = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();
        List<Query> querys = build.expand(new Query(query));
        return querys;
    }
}
