package com.x.xaiagent.vo;

import lombok.Data;

/**
 * 创建支付单返回：供前端拉起收银台/展示二维码。
 */
@Data
public class PayCreateVO {

    /** 支付单号（后续轮询状态用） */
    private String orderId;

    /**
     * 支付二维码（data URL：data:image/png;base64,...），前端直接 <img :src> 渲染。
     * 由后端用支付宝返回的 qr 原串生成 PNG 再 base64，无需前端引二维码库。
     */
    private String qrCodeUrl;

    /** 支付金额（元，字符串便于前端直接展示） */
    private String amount;

    /** 订单状态：WAIT_PAY / PAID / CLOSED */
    private String status;
}
