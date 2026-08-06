package com.x.xaiagent.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.key}")
    private String apiKey;

    @Test
    void testSearchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String result = webSearchTool.searchWeb("今日热点新闻");
        Assertions.assertNotNull(result);
        System.out.println(result);
    }
}