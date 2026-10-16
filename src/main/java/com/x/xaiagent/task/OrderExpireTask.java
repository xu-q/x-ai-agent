package com.x.xaiagent.task;

import com.x.xaiagent.config.AlipayProperties;
import com.x.xaiagent.entity.PointsRechargeOrder;
import com.x.xaiagent.mapper.PointsRechargeOrderMapper;
import com.x.xaiagent.service.PayService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动关单任务。
 * <p>当面付（precreate）的超时关闭支付宝不会发异步通知，必须由本任务主动扫描置 EXPIRED；
 * 同时配合查询接口惰性关单、预下单 timeout_express 兜底，三道防线保证不出现“永久待支付”。</p>
 */
@Component
public class OrderExpireTask {

    @Resource
    private PointsRechargeOrderMapper rechargeOrderMapper;

    @Resource
    private PayService payService;

    @Resource
    private AlipayProperties alipayProperties;

    /** 每 60 秒扫描一次超时未付订单并关单 */
    @Scheduled(fixedDelay = 60_000)
    public void closeExpired() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(alipayProperties.getOrderTimeoutMinutes());
        List<PointsRechargeOrder> expired = rechargeOrderMapper.selectExpiredWaitPay(deadline);
        for (PointsRechargeOrder order : expired) {
            payService.expireOrder(order.getOrderNo());
        }
    }
}
