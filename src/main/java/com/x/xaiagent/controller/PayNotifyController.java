package com.x.xaiagent.controller;

import com.x.xaiagent.service.PayService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付渠道异步回调（无需登录，需在拦截器放行）。
 * 路径前缀 /pay（context-path /api 下即 /api/pay）。
 */
@RestController
@RequestMapping("/pay")
public class PayNotifyController {

    @Resource
    private PayService payService;

    /**
     * 支付宝异步回调。无论内部是否成功处理，均返回 "success" 让支付宝停止重试；
     * 实际入账在 payService 内做幂等处理。
     */
    @PostMapping("/notify/alipay")
    public String alipayNotify(HttpServletRequest request) {
        try {
            payService.handleAlipayNotify(request);
        } catch (Exception e) {
            // 记录日志即可，仍返回 success 避免支付宝无限重试
        }
        return "success";
    }
}
