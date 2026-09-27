package com.yupi.yup_agent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
@SpringBootTest
class YuManusTest {

    @Resource
    private YuManus yuManus;

    @Test
    public void run() {
        String userPrompt = """
                我的吃饭搭子居住在上海市徐汇区徐家汇街道,
                请帮我找到任意3个离徐家汇近的烤肉店地点,
                附上地点图片的链接,把图片下载到本地,
                制定一份详细的聚餐计划,
                并以 PDF 格式输出,直接执行
                """;
        String answer = yuManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}