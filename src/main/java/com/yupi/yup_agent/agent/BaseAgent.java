package com.yupi.yup_agent.agent;

import cn.hutool.core.util.StrUtil;
import com.yupi.yup_agent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @ Gareth Bale
 * @ version 1.0
 */

/*
 * 抽象基础代理类,用于管理代理状态和执行流程
 *
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能
 * 子类必须实现step方法
 * */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 系统提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 最多执行几步
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型 SpringAi自带
    private ChatClient chatClient;

    // Memory记忆 (需要自主维护上下文)
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 执行,更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        // 执行循环
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i+1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}",stepNumber,maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                //用List记录执行所有步的结果
                results.add(result);
            }
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent",e);
            return "执行错误" + e.getMessage();
        } finally {
            this.cleanup();
        }
        // 检查是否超出步骤限制
        if (currentStep >= maxSteps){
            state = AgentState.FINISHED;
            results.add("Terminated: Reached max steps (" + maxSteps + ")");
        }
        return String.join("\n",results);
    }

    /**
     * 运行代理(流式输出)
     * @param userPrompt
     * @return
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L);// 3分钟超时
        // 使用线程异步处理，避免主线程阻塞
        CompletableFuture.runAsync(() -> {
            try {
                // 基础校验
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("错误，无法从状态运行代理: " + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("错误，不能使用空提示词运行代理: " + this.state);
                    sseEmitter.complete();
                    return;
                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
            // 执行,更改状态
            this.state = AgentState.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            // 执行循环
            try {
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i+1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}",stepNumber,maxSteps);
                    // 单步执行
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    //用List记录执行所有步的结果
                    results.add(result);
                    //输出当前每一步的结果到SSE
                    sseEmitter.send(result);
                }
                // 检查是否超出步骤限制
                if (currentStep >= maxSteps){
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    sseEmitter.send("执行结束: 达到最大步骤(" + maxSteps + ")");
                }
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent",e);
                try {
                    sseEmitter.send("执行错误: " + e.getMessage());
                    sseEmitter.complete();// 服务端主动结束这次sse连接
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                this.cleanup();
            }
        });
        // 设置超时时间
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });

        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING){
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }


    /**
     * 定义单个步骤
     *
     * @return
     */
    public abstract String step();

    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}
