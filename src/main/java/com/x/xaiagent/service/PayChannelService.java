package com.x.xaiagent.service;

import com.x.xaiagent.entity.PayOrder;
import com.x.xaiagent.vo.PayCreateVO;
import com.x.xaiagent.vo.PayNotifyResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 支付渠道抽象：每种渠道（支付宝/微信…）实现本接口。
 * <p>新增支付方式只需新增一个实现类（并在枚举/常量登记 channel），编排层 PayService 零改动。</p>
 */
public interface PayChannelService {

    /** 渠道标识，需与 PayConstants.CHANNEL_* 一致 */
    String channel();

    /** 创建支付：预下单并返回前端所需凭据（如扫码支付的二维码 data URL） */
    PayCreateVO createPay(PayOrder order);

    /** 解析异步回调：验签 + 提取订单号/交易号/支付状态 */
    PayNotifyResult handleNotify(HttpServletRequest request);
}
