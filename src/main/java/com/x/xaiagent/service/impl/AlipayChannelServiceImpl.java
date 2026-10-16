package com.x.xaiagent.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alibaba.fastjson.JSONObject;
import com.x.xaiagent.config.AlipayProperties;
import com.x.xaiagent.constant.PayConstants;
import com.x.xaiagent.entity.PayOrder;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.service.PayChannelService;
import com.x.xaiagent.util.QrCodeUtil;
import com.x.xaiagent.vo.PayCreateVO;
import com.x.xaiagent.vo.PayNotifyResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝渠道实现：扫码支付（当面付 alipay.trade.precreate）。
 * 预下单返回 qr_code 原串，由 {@link QrCodeUtil} 转成 PNG data URL 给前端直接渲染。
 */
@Service
public class AlipayChannelServiceImpl implements PayChannelService {

    @Resource
    private AlipayProperties props;

    private AlipayClient client;

    @PostConstruct
    public void init() {
        client = new DefaultAlipayClient(
                props.getGateway(),
                props.getAppId(),
                props.getPrivateKey(),
                props.getFormat(),
                props.getCharset(),
                props.getAlipayPublicKey(),
                props.getSignType());
    }

    @Override
    public String channel() {
        return PayConstants.CHANNEL_ALIPAY;
    }

    @Override
    public PayCreateVO createPay(PayOrder order) {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl(props.getNotifyUrl());

        JSONObject biz = new JSONObject();
        biz.put("out_trade_no", order.getOrderNo());
        biz.put("total_amount", order.getAmount().toPlainString());
        biz.put("subject", order.getSubject());
        // 超时表达式：超期后支付宝侧二维码自动失效（当面付超时关闭不推送通知，仅兜底防误付）
        biz.put("timeout_express", props.getOrderTimeoutMinutes() + "m");
        request.setBizContent(biz.toJSONString());

        try {
            AlipayTradePrecreateResponse response = client.execute(request);
            if (!response.isSuccess()) {
                throw new BusinessException("支付宝预下单失败：" + response.getSubMsg());
            }
            String qrContent = response.getQrCode();
            // 原串回写实体，便于后续轮询重渲染
            order.setQrCode(qrContent);

            PayCreateVO vo = new PayCreateVO();
            vo.setOrderId(order.getOrderNo());
            vo.setQrCodeUrl(QrCodeUtil.toDataUrl(qrContent));
            vo.setAmount(order.getAmount().toPlainString());
            vo.setStatus(order.getStatus());
            return vo;
        } catch (AlipayApiException e) {
            throw new BusinessException("支付宝预下单异常：" + e.getMessage());
        }
    }

    @Override
    public PayNotifyResult handleNotify(HttpServletRequest request) {
        PayNotifyResult result = new PayNotifyResult();
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        try {
            boolean verified = AlipaySignature.rsaCheckV1(
                    params, props.getAlipayPublicKey(), props.getCharset(), props.getSignType());
            result.setVerified(verified);
            if (!verified) {
                return result;
            }
            String tradeStatus = params.get("trade_status");
            result.setOrderNo(params.get("out_trade_no"));
            result.setTradeNo(params.get("trade_no"));
            result.setPaidAmount(new BigDecimal(params.get("total_amount")));
            result.setPaid("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus));
        } catch (AlipayApiException e) {
            result.setVerified(false);
        }
        return result;
    }

    @Override
    public void closeOrder(String outTradeNo) {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject biz = new JSONObject();
        biz.put("out_trade_no", outTradeNo);
        request.setBizContent(biz.toJSONString());
        try {
            AlipayTradeCloseResponse response = client.execute(request);
            if (!response.isSuccess()) {
                throw new BusinessException("支付宝关单失败：" + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            throw new BusinessException("支付宝关单异常：" + e.getMessage());
        }
    }
}
