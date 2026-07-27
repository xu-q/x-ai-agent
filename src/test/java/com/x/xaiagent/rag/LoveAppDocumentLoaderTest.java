package com.x.xaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class LoveAppDocumentLoaderTest {

    @Resource
    private LoveAppDocumentLoader LoveAppDocumentLoader;

    @Test
    void loadDocuments() throws IOException {
        List<Document> documents = LoveAppDocumentLoader.loadDocuments();
        System.out.println(documents.size());
    }

}