package com.x.xaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapTool webScrapTool = new WebScrapTool();
        String url = "https://www.baidu.com";
        String result = webScrapTool.scrapeWebPage(url);
        Assertions.assertNotNull(result);
        System.out.println(result);

    }

}