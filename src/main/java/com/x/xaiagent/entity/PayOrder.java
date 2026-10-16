package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 通用支付单：与具体业务解耦。
 * 后续接入微信等支付时复用同一张表，仅 channel / biz_type 不同。
 */
@Data
@TableName("pay_order")
public class PayOrder {

    /** 主键，UUID 字符串（Java 端生成，IdType.INPUT） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 支付单号（业务唯一），同时作为渠道 out_trade_no */
    private String orderNo;

    /** 下单用户 ID */
    private String userId;

    /** 支付渠道：ALIPAY / WECHAT */
    private String channel;

    /** 业务类型：POINTS_RECHARGE */
    private String bizType;

    /** 关联业务单号，等于 points_recharge_order.order_no */
    private String bizNo;

    /** 支付金额（元） */
    private BigDecimal amount;

    /** 订单标题（支付页展示用） */
    private String subject;

    /** 支付状态：WAIT_PAY / PAID / CLOSED */
    private String status;

    /** 渠道交易流水号（支付宝交易号等） */
    private String channelTradeNo;

    /** 预下单返回的二维码原串 */
    private String qrCode;

    /** 回调原始报文，用于对账 */
    private String notifyBody;

    /** 支付完成时间 */
    private LocalDateTime paidTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 逻辑删除：0 未删，1 已删 */
    @TableLogic
    private Integer deleted;
}
