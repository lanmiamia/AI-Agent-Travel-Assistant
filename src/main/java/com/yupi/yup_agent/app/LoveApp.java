package com.yupi.yup_agent.app;

import com.yupi.yup_agent.advisor.MyLoggerAdvisor;
import com.yupi.yup_agent.chatmemory.FileBasedChatMemory;
import com.yupi.yup_agent.rag.LoveAppRagCustomAdvisorFactory;
import com.yupi.yup_agent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @ Gareth Bale
 * @ version 1.0
 */


// 单元测试是否能跑通

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT="你是一位细心而博学的旅行生活家。请以启发式提问为主，每轮回复先共鸣用户的旅行情绪或期待，再抛出2-3个开放式问题，逐步引导用户从\"想去哪里\"深入到\"想感受什么\"和\"想成为怎样的自己\"，帮助用户在对话中理清真正的旅行渴望。避免直接给固定攻略，只在关键节点提供在地化的人文视角或小众体验建议，让用户感到被理解和充满出发的灵感。";

    //构造方法
    public LoveApp(ChatModel dashscopeChatModel){
        //初始化基于文件的对话记忆
        //System.getProperty("user.dir")获取当前程序运行的工作目录
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";

        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // //初始化基于内存的对话记忆
        // ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor, 可按需开启
                        new MyLoggerAdvisor()
                )
                .build();
    }

    //向AI提问,返回字符串回答
    public String doChat(String message, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()                                              //开始一次新的对话请求
                .user(message)                                         //用户当前说的话
                .advisors(spec -> spec                                 //配置顾问参数
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) //会话ID,用于区分不同参数
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))    //只保存最早的10条上下文记录(并非最近)
                .call()                                                //调用AI模型
                .chatResponse();                                       //获取响应对象

        //从响应中提取AI文本
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);                               //打印日志
        return content;
    }


    /**
     * SSE流式传输
     * @param message
     * @param chatId
     * @return
     */
    //向AI提问,返回字符串回答
    public Flux<String> doChatByStream(String message, String chatId){
        // ChatResponse 包含了List<generation>包含了ChatGenerationMetadata包含了这次回复所消耗的Token
        // 调用content只返回AI输出的文本信息, 而不返回整个对象ChatResponse，减少带宽消耗
        // 立刻返回，等待返回的同时执行后续语句
        return chatClient
                .prompt()                                              //开始一次新的对话请求
                .user(message)                                         //用户当前说的话
                .advisors(spec -> spec                                 //配置顾问参数
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) //会话ID,用于区分不同参数
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))    //只保存最早的10条上下文记录(并非最近)
                .stream()                                                //调用AI模型
                .content();//获取响应对象
    }

    // 向AI提问，返会自定义结构的文本回答
    // record 关键字 快速定义类(以构造方法的语法) 字段默认final类型
    record LoveReport(String title, List<String> suggestions){}

    public LoveReport doChatWithReport(String message, String chatId){
        LoveReport loveReport = chatClient
                .prompt()//开始一次新的对话请求
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果,标题为{用户名}的恋爱报告,内容为建议列表")
                .user(message)                                         //用户当前说的话
                .advisors(spec -> spec                                 //配置顾问参数
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) //会话ID,用于区分不同房间参数
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))    //只保存最早的10条上下文记录(并非最近)
                .call()                                                //调用AI模型
                .entity(LoveReport.class);                                     //获取响应对象

        //从响应中提取AI文本
        log.info("loveReport: {}",loveReport);                               //打印日志
        return loveReport;
    }

    // AI 恋爱知识库RAG问答功能,VectorStore内存向量数据库;
    // 非永久保存,每次提问时都执行自动读md文档,写进VectorStore,作为context和用户提问一并输入大模型

    // @Resource 根据名称注入
    @Resource
    private VectorStore loveAppVectorStore;

    /**
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                //spec是AdvisorSpec接口, 内部有param这个未实现的抽象方法
                //Consumer<T>是链式调用java.util类
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 知识库问答
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    @Resource
    private Advisor loveAppRagCloudAdvisor;
    public String doChatWithRagCloud(String message, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                //spec是AdvisorSpec接口, 内部有param这个未实现的抽象方法
                //Consumer<T>是链式调用java.util类
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 检索增强生成服务(基于云知识库服务)
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }


    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    public String doChatWithRagPGVector(String message, String chatId){
        // 得到查询重写后的查询信息
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                //spec是AdvisorSpec接口, 内部有param这个未实现的抽象方法
                //Consumer<T>是链式调用java.util类
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 检索增强生成服务(基于云知识库服务)
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用 自定义 查询检索器 设置
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor
                        (pgVectorVectorStore,"海边"))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }


    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()//开始一次新的对话请求
                .user(message)                                         //用户当前说的话
                .advisors(spec -> spec                                 //配置顾问参数
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) //会话ID,用于区分不同房间参数
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))    //只保存最早的10条上下文记录(并非最近)
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()                                                //调用AI模型
                .chatResponse();//获取响应对象

        //从响应中提取AI文本
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    // AI调用MCP服务
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMCP(String message, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()//开始一次新的对话请求
                .user(message)                                         //用户当前说的话
                .advisors(spec -> spec                                 //配置顾问参数
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId) //会话ID,用于区分不同房间参数
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))    //只保存最早的10条上下文记录(并非最近)
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()                                                //调用AI模型
                .chatResponse();//获取响应对象

        //从响应中提取AI文本
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

}
