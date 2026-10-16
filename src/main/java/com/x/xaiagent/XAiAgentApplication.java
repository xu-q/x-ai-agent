package com.x.xaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.x.xaiagent.mapper")
@EnableScheduling
public class XAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(XAiAgentApplication.class, args);
    }
}
