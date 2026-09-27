package com.yupi.yup_agent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
// 先在cmd命令行中输入ollama gemm3:1b启动本地模型 再启动Application后自动启动
//用Ollama时解除注释
//@Component
public class OllamaAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel ollamaChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage assistantMessage = ollamaChatModel.call(new Prompt("你好,我是鱼皮"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }

}
