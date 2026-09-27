package com.yupi.yup_agent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
// 启动SpringbootApplication后启动main
public class LangChainAiInvoke {
    public static void main(String[] args) {
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String answer = qwenChatModel.chat("我是程序员鱼皮,这是编程导航codefather.cn的AI 超级智能体原创项目");
        System.out.println(answer);
    }
}
