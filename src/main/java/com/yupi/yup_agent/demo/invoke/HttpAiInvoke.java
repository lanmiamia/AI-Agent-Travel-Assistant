package com.yupi.yup_agent.demo.invoke;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

public class HttpAiInvoke {

    public static void main(String[] args) {
        // 阿里云百炼平台创建的API_KEY
        String apiKey = TestApiKey.API_KEY; // 或直接替换为你的 API Key
        // 请求网址
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 1. 构建 messages 数组
        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.set("role", "system");
        systemMsg.set("content", "You are a helpful assistant.");

        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", "你是谁？");

        messages.add(systemMsg);
        messages.add(userMsg);

        // 2. 构建 input 对象
        JSONObject input = new JSONObject();
        input.set("messages", messages);

        // 3. 构建 parameters 对象
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");

        // 4. 构建顶层请求体 Body
        JSONObject bodyJson = new JSONObject();
        bodyJson.set("model", "qwen-plus");
        bodyJson.set("input", input);
        bodyJson.set("parameters", parameters);

        // 5. 发送 POST 请求
        String responseBody = HttpRequest.post(url)
                .auth("Bearer " + apiKey)
                .contentType("application/json")
                .body(bodyJson.toString())
                .execute()
                .body();

        System.out.println("响应结果：" + responseBody);
    }
}