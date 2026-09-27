package com.yupi.yup_agent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * @ Gareth Bale
 * @ version 1.0
 */

/* 创建上下文查询增强器的工厂 */
public class LoveAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance(){
        PromptTemplate emptyContextpromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答旅游相关的问题，别的没办法帮到您哦，
                有问题可以联系编程导航客服 https://codefather.cn
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false) //不允许空上下文
                .emptyContextPromptTemplate(emptyContextpromptTemplate) //上下文为空时使用自定义提示词模板
                .build();
    }
}




