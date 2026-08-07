package com.x.xaiagent.app;

import com.x.xaiagent.advisor.MyLoggerAdvisor;
import com.x.xaiagent.rag.LoveAppRagCloudAdvisorConfig;
import com.x.xaiagent.rag.LoveAppRagCustomAdvisorFactory;
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
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

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
     * AI对话 支持多轮对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
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
     * 基于本地知识库问答
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
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
     * 基于Pg数据库 知识库 rag 检索增加
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRagPg(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
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
}
