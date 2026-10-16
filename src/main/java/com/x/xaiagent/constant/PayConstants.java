package com.x.xaiagent.constant;

/**
 * 支付模块通用常量。
 */
public final class PayConstants {

    private PayConstants() {
    }

    // ===================== 支付渠道 =====================
    /** 支付宝 */
    public static final String CHANNEL_ALIPAY = "ALIPAY";
    /** 微信支付（预留，后续接入） */
    public static final String CHANNEL_WECHAT = "WECHAT";

    // ===================== 业务类型 =====================
    /** 积分充值 */
    public static final String BIZ_POINTS_RECHARGE = "POINTS_RECHARGE";

    // ===================== 订单状态 =====================
    /** 待支付 */
    public static final String STATUS_WAIT_PAY = "WAIT_PAY";
    /** 已支付（回调验签通过且积分已入账） */
    public static final String STATUS_PAID = "PAID";
    /** 已取消（用户主动取消 / 商户冲正关闭） */
    public static final String STATUS_CANCELLED = "CANCELLED";
    /** 已超时（超过有效时长未付，自动关单） */
    public static final String STATUS_EXPIRED = "EXPIRED";
}
