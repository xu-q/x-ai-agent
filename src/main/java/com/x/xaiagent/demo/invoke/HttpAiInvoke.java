package com.x.xaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpAiInvoke {

    public static void main(String[] args) {
// 1. 基础参数
        String url = "https://llm-x3b5cx6kt8lxu8bt.cn-beijing.maas.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
        String token = "sk-ws-H.EHPMPDM.QFxv.MEUCIQCGyyfc5jiGSgdUhyXs1CivFhiDaGHcLM6wairO1HOokgIgaapgrtbxM7x1jJz-5HJfCZ5mtK_sY2H1GfWZDgOciwU";

// 2. 构建 content 数组（包含图片和文本）
        List<Map<String, String>> contentList = new ArrayList<>();
        Map<String, String> imageItem = new HashMap<>();
        imageItem.put("image", "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg");
        contentList.add(imageItem);

        Map<String, String> textItem = new HashMap<>();
        textItem.put("text", "图中描绘的是什么景象?");
        contentList.add(textItem);

// 3. 构建 message 对象（使用 HashMap 避免 MapUtil.of 的类型推断问题）
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", contentList);

// 4. 构建 messages 数组
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);

// 5. 构建 input 对象
        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);

// 6. 构建最外层请求体 requestBody
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen3.7-plus");
        requestBody.put("input", input);

// 7. Hutool HttpRequest 发起 POST 请求
        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(requestBody))
                .timeout(30000)
                .execute();

// 8. 响应处理
        if (response.isOk()) {
            String respJson = response.body();
            System.out.println("接口返回结果：");
            System.out.println(JSONUtil.formatJsonStr(respJson));
        } else {
            System.err.println("请求失败，状态码：" + response.getStatus());
            System.err.println("错误响应：" + response.body());
        }
    }
}
