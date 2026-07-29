package com.x.xaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MultiQueryExpandderDemoTest {

    @Resource
    private MultiQueryExpandderDemo multiQueryExpandderDemo;

    @Test
    void expand() {
        List<Query> querys = multiQueryExpandderDemo.expand("程序员");
        System.out.println(querys);
    }
}