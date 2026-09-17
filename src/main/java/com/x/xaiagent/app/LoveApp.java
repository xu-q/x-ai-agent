package com.x.xaiagent.app;

import com.x.xaiagent.advisor.MyLoggerAdvisor;
import com.x.xaiagent.entity.ChatMessage;
import com.x.xaiagent.rag.LoveAppRagCloudAdvisorConfig;
import com.x.xaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.x.xaiagent.service.ChatMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    @Resource
    private ChatMessageService chatMessageService;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份（只有第一条对话时候才表明），告知用户可倾诉恋爱难题。" +
            "可以围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "当用户偏移情感相关话题的时候可以尝试去引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 初始化AI客户端
     *
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // ✅ 基于内存的 ChatMemory（默认使用 InMemoryChatMemoryRepository）
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)  // 保留最近 10 条消息
                .build();

       /* //基于文件的 ChatMemory
        String baseDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(baseDir);*/

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * AI对话 支持多轮对话（自动落库 用户+助手消息）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        String conversationId = resolveConversationId(chatId);
        chatMessageService.saveMessage(buildMsg("user", message, conversationId));
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        chatMessageService.saveMessage(buildMsg("assistant", content, conversationId));
        return content;
    }

    /**
     * AI对话 支持多轮对话 流式输出（自动落库 用户+助手消息）
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        String conversationId = resolveConversationId(chatId);
        chatMessageService.saveMessage(buildMsg("user", message, conversationId));
        Flux<String> flux = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
        StringBuilder sb = new StringBuilder();
        return flux.doOnNext(sb::append)
                .doOnComplete(() ->
                        chatMessageService.saveMessage(buildMsg("assistant", sb.toString(), conversationId)));
    }


    record LoveReport(String title, List<String> suggestions) {
    }


    /**
     * Ai 恋爱报告，结构化输出
     *
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }


    @Resource
    private VectorStore loveAppVectorStore;

    /**
     * 基于本地知识库问答（自动落库 用户+助手消息+引用知识片段）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        String conversationId = resolveConversationId(chatId);
        chatMessageService.saveMessage(buildMsg("user", message, conversationId));
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用本地知识库问答
                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                //按照条件去匹配，比如问题是已婚类的问题，status是单身，将匹配不到相关内容
                //.advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, "单身"))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        ChatMessage assistant = chatMessageService.saveMessage(buildMsg("assistant", content, conversationId));
        // 默认 QuestionAnswerAdvisor 不回传引用，这里主动用同一向量库检索本次命中的知识片段并落库
        List<Document> citations = retrieveCitations(loveAppVectorStore, message);
        chatMessageService.saveCitations(assistant.getId(), citations);
        return content;
    }


    @Resource
    private Advisor loveAppRagCloudAdvisor;

    /**
     * 基于云服务的知识库 rag 检索增加
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRagCloud(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用阿里云百炼知识库
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }


    @Resource
    private VectorStore vectorStore;

    /**
     * 基于Pg数据库 知识库 rag 检索增加（自动落库 用户+助手消息+引用知识片段）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRagPg(String message, String chatId) {
        String conversationId = resolveConversationId(chatId);
        chatMessageService.saveMessage(buildMsg("user", message, conversationId));
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        ChatMessage assistant = chatMessageService.saveMessage(buildMsg("assistant", content, conversationId));
        List<Document> citations = retrieveCitations(vectorStore, message);
        chatMessageService.saveCitations(assistant.getId(), citations);
        return content;
    }


    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        return content;
    }

    // ==================== 落库辅助方法 ====================

    private ChatMessage buildMsg(String role, String content, String conversationId) {
        ChatMessage msg = new ChatMessage();
        msg.setRole(role);
        msg.setContent(content);
        msg.setConversationId(conversationId);
        return msg;
    }

    /**
     * 解析会话ID：为空时生成 UUID，保证同一轮 user/assistant 落到同一会话
     */
    private String resolveConversationId(String chatId) {
        return (chatId == null || chatId.isBlank()) ? UUID.randomUUID().toString() : chatId;
    }

    /**
     * 用向量库检索本次问题命中的知识片段（用于引用落库）；检索失败不影响主流程
     */
    private List<Document> retrieveCitations(VectorStore vectorStore, String query) {
        try {
            return vectorStore.similaritySearch(SearchRequest.builder()
                    .query(query)
                    .topK(3)
                    .similarityThreshold(0.5)
                    .build());
        } catch (Exception e) {
            log.warn("检索引用知识片段失败，跳过引用落库", e);
            return List.of();
        }
    }
}
