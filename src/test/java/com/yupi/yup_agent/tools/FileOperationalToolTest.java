package com.yupi.yup_agent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
@SpringBootTest
class FileOperationalToolTest {

    @Test
    void readFile() {
        FileOperationalTool fileOperationalTool = new FileOperationalTool();
        String fileName = "编程导航.txt";
        String result = fileOperationalTool.readFile(fileName);
        Assertions.assertNotNull(result);
    }

    @Test
    void writeFile() {
        FileOperationalTool fileOperationalTool = new FileOperationalTool();
        String fileName = "编程导航.txt";
        String content = "https://www.codefather.cn 程序员编程学习交流社区";
        String result = fileOperationalTool.writeFile(fileName,content);
        Assertions.assertNotNull(result);
    }
}