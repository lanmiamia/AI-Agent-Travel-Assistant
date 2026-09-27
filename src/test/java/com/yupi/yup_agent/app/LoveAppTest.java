package com.yupi.yup_agent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ Gareth Bale
 * @ version 1.0
 */

//测试是否能跑通
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    // 测试多轮对话上下文是否能记住
    @Test
    void doChat() {
        //更改 记忆的数量10->1 测试是否有效
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮";
        String answer = loveApp.doChat(message,chatId);
        //第二轮
        message = "我想让另一半(编程导航)更爱我";
        answer = loveApp.doChat(message,chatId);
        Assertions.assertNotNull(answer);
        //第三轮
        message = "我的另一半叫什么来着?刚跟你说过,帮我回忆一下";
        answer = loveApp.doChat(message,chatId);
        Assertions.assertNotNull(answer);
    }

    //自定义返回格式,不用提供的chatResponse
    @Test
    void doChatWithReport() {
        //String chatId = UUID.randomUUID().toString();
        String chatId = "111";
        String message = "你好,我是程序员鱼皮,我想让另一半(编程导航)更爱我,但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "在草原上骑马有哪些注意事项？";
        String answer = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithRagCloud() {
        String chatId = UUID.randomUUID().toString();
        String message = "在草原上骑马有哪些注意事项？";
        String answer = loveApp.doChatWithRagCloud(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithRagPGVector() {
        String chatId = UUID.randomUUID().toString();
        String message = "在草原上骑马有哪些注意事项？";
        String answer = loveApp.doChatWithRagPGVector(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("周末想和好朋友去上海旅游，推荐几个适合拍照的小众打卡地,按照你的理解就行，直接执行");

        // 测试网页抓取：恋爱案例分析
        testMessage("最近失业了，看看编程导航网站(codefather.cn)的其他人员是怎么寻找办法的？按照你的理解就行，直接执行");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的球星赛场图片为文件,什么图片都行，直接下载");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告，按照你的理解就行，直接执行");

        // 测试文件操作：保存用户档案
        testMessage("保存 我的旅游计划 这6个字 为文件，按照你的理解，直接执行");

        // 测试 PDF 生成
        testMessage("生成一份'国庆旅游计划'PDF，包含酒店预定、活动流程和纪念品清单，按照你的理解就行，直接执行");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithMCP() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图MCP
        // String message = "我的吃饭搭子居住在上海市徐汇区徐家汇街道,请帮我找到任意3个离徐家汇近的烤肉店地点,附上地点图片的链接,直接执行";
        // String answer = loveApp.doChatWithMCP(message,chatId);
        // Assertions.assertNotNull(answer);

        // 测试图片搜索MCP
        String message = "帮我搜索一些老师给学生解答问题的图片,没有具体要求,直接给链接";
        String answer = loveApp.doChatWithMCP(message,chatId);
        Assertions.assertNotNull(answer);

    }
}