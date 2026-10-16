package com.x.xaiagent.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建积分充值订单入参。
 * <p>积分入账值由后端按档位/汇率计算，不信任前端传入的 points，避免越权刷分。</p>
 */
@Data
public class CreateRechargeOrderDTO {

    /**
     * 档位 key（对应 PointsPackageConst 的 key）。
     * 为空时表示自定义金额充值（需传 amount）。
     */
    private String packageKey;

    /**
     * 自定义充值金额（元）。档位下单时此值忽略，以档位价格为准。
     * 仅当 packageKey 为空时生效，范围 1~1000。
     */
    private BigDecimal amount;

    /**
     * 支付渠道：ALIPAY / WECHAT，默认 ALIPAY。
     */
    private String channel;
}
