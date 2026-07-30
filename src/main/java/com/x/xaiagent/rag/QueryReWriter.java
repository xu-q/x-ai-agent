package com.x.xaiagent.rag;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 查询重写器（ai改写查询）
 */
@Component
public class QueryReWriter {

    private final QueryTransformer queryTransformer;

    public QueryReWriter(ChatModel dashscopeChatModel) {
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(dashscopeChatModel))
                .build();
    }
    public String rewrite(String query) {
        Query newQuery = queryTransformer.transform(new Query(query));
        return newQuery.text();
    }
}
