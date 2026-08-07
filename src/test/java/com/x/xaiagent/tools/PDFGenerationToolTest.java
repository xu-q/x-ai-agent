package com.x.xaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
         String result = tool.generatePDF("test.pdf", "测试数据");
         System.out.println(result);
         assertNotNull(result);
    }
}