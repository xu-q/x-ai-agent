package com.x.xaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QueryReWriterTest {

    @Resource
    private QueryReWriter queryReWriter;

    @Resource
    private ChatModel dashscopeChatModel;

    @Test
    void rewrite() {
        /*QueryReWriter queryReWriter1 = new QueryReWriter(dashscopeChatModel);
        String rewrite = queryReWriter1.rewrite("程序员");*/

        String rewrite = queryReWriter.rewrite("我已经结婚了，但是婚后生活不幸福，我应该怎么办？");
        System.out.println(rewrite);
    }
}