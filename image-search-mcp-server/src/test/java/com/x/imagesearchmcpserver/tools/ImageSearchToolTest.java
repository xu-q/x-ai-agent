package com.x.imagesearchmcpserver.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {

    @Test
    void searchImages() {
        ImageSearchTool imageSearchTool = new ImageSearchTool();
        String result = imageSearchTool.searchImages("电脑壁纸尺寸的性感美女", 15, 1);
        System.out.println(result);
    }
}