package com.yupi.yup_agent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @ Gareth Bale
 * @ version 1.0
 */

/*
* ReAct(Reasoning and Acting) 模式的代理抽象类
* 实现了思考-行动的循环模式
* */

// callSuper = true 同时调用父类的equals方法，即父类和子类的成员要都一样才行
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent{


    /**
     * 处理当前状态并决定下一步行动,true表示需要执行,false表示不需要执行
     * @return
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return
     */
    public abstract String act();

    /**
     * 执行单个步骤,思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            // 先思考
            boolean shouldAct = think();
            if (!shouldAct){
                return "思考完成 - 无需行动";
            }
            // 再行动
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
