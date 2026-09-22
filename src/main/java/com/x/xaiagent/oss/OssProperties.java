package com.x.xaiagent.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置绑定（前缀 app.oss）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.oss")
public class OssProperties {

    /** OSS endpoint，如 oss-cn-hangzhou.aliyuncs.com（可带或不带 https://） */
    private String endpoint;

    /** bucket 名称 */
    private String bucketName;

    /** RAM 子账号 AccessKeyId */
    private String accessKeyId;

    /** RAM 子账号 AccessKeySecret */
    private String accessKeySecret;

    /** 可选：自定义域名/CDN（仅公共读生效） */
    private String domain;

    /** 上传根路径，结尾建议带 / */
    private String basePath = "upload/";

    /** 是否私有 bucket：true 返回签名临时 URL，false 返回直链 */
    private Boolean isPrivate = true;

    /** 私有 bucket 签名 URL 有效期（分钟） */
    private long urlExpirationMinutes = 60;
}
