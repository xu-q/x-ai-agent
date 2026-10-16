package com.x.xaiagent.service;

import com.x.xaiagent.dto.CreateRechargeOrderDTO;
import com.x.xaiagent.vo.PayCreateVO;
import com.x.xaiagent.vo.RechargeStatusVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 支付编排服务：对业务层屏蔽渠道差异，统一建单/回调入账/状态查询。
 */
public interface PayService {

    /** 创建积分充值订单（建充值单 + 支付单 + 调渠道预下单） */
    PayCreateVO createRechargeOrder(CreateRechargeOrderDTO dto, String userId);

    /** 处理支付宝异步回调（验签 + 幂等入账） */
    void handleAlipayNotify(HttpServletRequest request);

    /** 查询充值订单状态（前端轮询） */
    RechargeStatusVO getRechargeStatus(String orderNo);
}
