package com.x.xaiagent.vo;

import lombok.Data;

/**
 * 充值订单状态（前端轮询）。
 */
@Data
public class RechargeStatusVO {

    /** 支付单号 */
    private String orderId;

    /**
     * 状态：WAIT_PAY（待支付）/ PAID（已支付，积分已到账）/ CANCELLED（已取消）/ EXPIRED（已超时）。
     * 前端以 "PAID" 判定支付完成。
     */
    private String status;

    /** 已到账积分（支付成功后有值） */
    private Integer points;

    /** 支付二维码（data URL），刷新页面后重渲染用 */
    private String qrCodeUrl;
}
