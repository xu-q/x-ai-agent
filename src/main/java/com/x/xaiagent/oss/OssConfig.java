package com.x.xaiagent.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化阿里云 OSS 客户端 Bean。
 */
@Configuration
@RequiredArgsConstructor
public class OssConfig {

    private final OssProperties props;

    @Bean
    public OSS ossClient() {
        String endpoint = props.getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("未配置 app.oss.endpoint，请检查 application-local.yml");
        }
        if (!endpoint.startsWith("http")) {
            endpoint = "https://" + endpoint;
        }
        return new OSSClientBuilder().build(
                endpoint, props.getAccessKeyId(), props.getAccessKeySecret());
    }
}
