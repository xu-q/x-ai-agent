package com.x.xaiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置（对应 application-local.yml 的 app.alipay.*）。
 * 沙箱密钥请勿提交到 git。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.alipay")
public class AlipayProperties {

    /** 沙箱/正式 appId */
    private String appId;

    /** 应用私钥（PKCS8，Base64） */
    private String privateKey;

    /** 支付宝公钥（Base64） */
    private String alipayPublicKey;

    /** 网关地址（沙箱：https://openapi-sandbox.dl.alipaydev.com/gateway.do） */
    private String gateway;

    /** 异步回调公网地址（本地联调用 ngrok/cpolar 暴露） */
    private String notifyUrl;

    private String format = "json";

    private String charset = "UTF-8";

    private String signType = "RSA2";

    /** 订单超时时长（分钟）：超过未付则自动关单，并传给支付宝 timeout_express */
    private int orderTimeoutMinutes = 15;
}
