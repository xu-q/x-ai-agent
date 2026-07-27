package com.x.xaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

@Slf4j
public class MyLoggerAdvisor implements BaseAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        Prompt prompt = request.prompt();

        String userText = prompt.getUserMessage().getText();
        log.info("===== AI Request =====");
        log.info("=====userText: {}", userText);

        List<Message> messages = prompt.getInstructions();
        String systemText = messages.stream()
                .filter(m -> m instanceof SystemMessage)
                .map(Message::getText)
                .findFirst()
                .orElse(null);
        //log.info("=====systemText: {}", systemText);

        /*log.info("=====chatOptions: {}", prompt.getOptions());
        log.info("=====messages: {}", messages);
        log.info("=====media: {}", prompt.getUserMessage().getMedia());*/

        Map<String, Object> context = request.context();
        //log.info("=====context (advisorParams): {}", context);

        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        log.info("===== AI Response =====");
        log.info("=====content: {}", response.chatResponse().getResult().getOutput().getText());
        //log.info("=====metadata: {}", response.chatResponse().getResult().getMetadata());
        return response;
    }
}
