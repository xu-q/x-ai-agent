package com.x.xaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

public class Langchain4jAiInvoke {

        public static void main(String[] args) {
            QwenChatModel build = QwenChatModel.builder()
                    .apiKey(TestApiKey.API_KEY)
                    .modelName("qwen-max")
                    .build();

            String answer = build.chat("我是");
            System.out.println(answer);
        }
}
