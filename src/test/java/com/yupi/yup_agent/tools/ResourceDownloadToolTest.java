package com.yupi.yup_agent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
class ResourceDownloadToolTest {

    @Test
    void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://www.codefather.cn/logo.png";
        String fileName = "logo.png";
        String result = tool.downloadResource(url,fileName);
        assertNotNull(result);
    }
}