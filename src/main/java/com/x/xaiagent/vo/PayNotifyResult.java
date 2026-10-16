package com.x.xaiagent.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 渠道回调解析结果（与具体渠道解耦的通用结构）。
 */
@Data
public class PayNotifyResult {

    /** 签名校验是否通过 */
    private boolean verified;

    /** 交易是否已支付成功（TRADE_SUCCESS / TRADE_FINISHED） */
    private boolean paid;

    /** 商户订单号（out_trade_no） */
    private String orderNo;

    /** 渠道交易流水号 */
    private String tradeNo;

    /** 实际支付金额（元） */
    private BigDecimal paidAmount;
}
