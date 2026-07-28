package com.x.xaiagent.rag;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LoveAppDocumentVectorStoreInit {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private VectorStore vectorStore;
    public void init() {
        List<Document> documents = loveAppDocumentLoader.loadDocuments();
        int batchSize = 10;
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            vectorStore.add(documents.subList(i, end));
        }
        log.info("---------------------init loveAppDocumentVectorStore 共" + documents.size() + "条数据");
    }
}
