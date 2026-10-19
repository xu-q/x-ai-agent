package com.x.xaiagent.service.impl;

import com.x.xaiagent.config.AlipayProperties;
import com.x.xaiagent.constant.PayConstants;
import com.x.xaiagent.constant.PointsPackageConst;
import com.x.xaiagent.constant.PointTransactionType;
import com.x.xaiagent.dto.CreateRechargeOrderDTO;
import com.x.xaiagent.entity.PayOrder;
import com.x.xaiagent.entity.PointTransaction;
import com.x.xaiagent.entity.PointsRechargeOrder;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.mapper.PayOrderMapper;
import com.x.xaiagent.mapper.PointAccountMapper;
import com.x.xaiagent.mapper.PointTransactionMapper;
import com.x.xaiagent.mapper.PointsRechargeOrderMapper;
import com.x.xaiagent.service.PayChannelService;
import com.x.xaiagent.service.PayService;
import com.x.xaiagent.util.QrCodeUtil;
import com.x.xaiagent.vo.PayCreateVO;
import com.x.xaiagent.vo.PayNotifyResult;
import com.x.xaiagent.vo.RechargeStatusVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付编排实现。
 * <p>注入所有 PayChannelService 实现，按 channel 自动注册成 Map；
 * 后续新增微信等支付只需新增实现类，本类无需改动。</p>
 */
@Service
public class PayServiceImpl implements PayService {

    @Resource
    private PayOrderMapper payOrderMapper;

    @Resource
    private PointsRechargeOrderMapper rechargeOrderMapper;

    @Resource
    private PointTransactionMapper pointTransactionMapper;

    @Resource
    private PointAccountMapper pointAccountMapper;

    @Resource
    private AlipayProperties alipayProperties;

    @Resource
    private List<PayChannelService> channelServices;

    private final Map<String, PayChannelService> channelMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (PayChannelService svc : channelServices) {
            channelMap.put(svc.channel(), svc);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO createRechargeOrder(CreateRechargeOrderDTO dto, String userId) {
        // 1. 解析金额与积分（积分以服务端为准，不信任前端）
        BigDecimal amount;
        int points;
        String packageKey = null;
        BigDecimal amt = dto.getAmount();
        if (dto.getPackageKey() != null && !dto.getPackageKey().isBlank() && amt == null) {
            PointsPackageConst.Def def = PointsPackageConst.get(dto.getPackageKey());
            if (def == null) {
                throw new BusinessException("档位不存在");
            }
            amount = def.price();
            points = def.totalPoints();
            packageKey = def.key();
        } else {
            if (amt == null || amt.compareTo(BigDecimal.ONE) < 0 || amt.compareTo(new BigDecimal("1000")) > 0) {
                throw new BusinessException("自定义充值金额需在 1~1000 元之间");
            }
            amount = amt;
            points = PointsPackageConst.customAmountToPoints(amt);
        }

        // 2. 渠道（默认支付宝）
        String channel = (dto.getChannel() == null || dto.getChannel().isBlank())
                ? PayConstants.CHANNEL_ALIPAY
                : dto.getChannel().toUpperCase();
        PayChannelService channelSvc = channelMap.get(channel);
        if (channelSvc == null) {
            throw new BusinessException("不支持的支付渠道：" + channel);
        }

        // 3. 建充值单 + 支付单
        String orderNo = genOrderNo();
        LocalDateTime now = LocalDateTime.now();

        PointsRechargeOrder ro = new PointsRechargeOrder();
        ro.setId(UUID.randomUUID().toString());
        ro.setOrderNo(orderNo);
        ro.setUserId(userId);
        ro.setPackageKey(packageKey);
        ro.setAmount(amount);
        ro.setPoints(points);
        ro.setStatus(PayConstants.STATUS_WAIT_PAY);
        ro.setCreateTime(now);
        ro.setDeleted(0);
        rechargeOrderMapper.insert(ro);

        PayOrder po = new PayOrder();
        po.setId(UUID.randomUUID().toString());
        po.setOrderNo(orderNo);
        po.setUserId(userId);
        po.setChannel(channel);
        po.setBizType(PayConstants.BIZ_POINTS_RECHARGE);
        po.setBizNo(orderNo);
        po.setAmount(amount);
        po.setSubject("积分充值 " + points + " 积分");
        po.setStatus(PayConstants.STATUS_WAIT_PAY);
        po.setCreateTime(now);
        po.setDeleted(0);
        payOrderMapper.insert(po);

        // 4. 调渠道预下单，拿到二维码并回写
        PayCreateVO vo = channelSvc.createPay(po);
        po.setUpdateTime(now);
        payOrderMapper.updateById(po);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayNotify(HttpServletRequest request) {
        PayChannelService svc = channelMap.get(PayConstants.CHANNEL_ALIPAY);
        PayNotifyResult notify = svc.handleNotify(request);
        // 验签失败或未支付成功：不入账；控制器仍返回 success，让支付宝停止重试
        if (!notify.isVerified() || !notify.isPaid()) {
            return;
        }
        markPaid(notify.getOrderNo(), notify.getTradeNo());
    }

    /**
     * 标记支付成功并幂等入账。重复回调安全：状态已 PAID 直接返回；
     * 流水 UNIQUE(user_id, biz_no) 兜底重复加积分。
     */
    private void markPaid(String orderNo, String tradeNo) {
        PayOrder po = payOrderMapper.selectByOrderNo(orderNo);
        if (po == null) {
            return;
        }
        if (PayConstants.STATUS_PAID.equals(po.getStatus())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        po.setStatus(PayConstants.STATUS_PAID);
        po.setChannelTradeNo(tradeNo);
        po.setPaidTime(now);
        po.setUpdateTime(now);
        payOrderMapper.updateById(po);

        PointsRechargeOrder ro = rechargeOrderMapper.selectByOrderNo(orderNo);
        if (ro != null && !PayConstants.STATUS_PAID.equals(ro.getStatus())) {
            ro.setStatus(PayConstants.STATUS_PAID);
            ro.setUpdateTime(now);
            rechargeOrderMapper.updateById(ro);
        }

        int points = ro != null ? ro.getPoints() : 0;

        PointTransaction tx = new PointTransaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setUserId(po.getUserId());
        tx.setPoints(points);
        tx.setType(PointTransactionType.RECHARGE);
        tx.setBizNo("RECHARGE:" + orderNo);
        tx.setRemark("积分充值");
        tx.setCreateTime(now);
        try {
            pointTransactionMapper.insert(tx);
        } catch (DuplicateKeyException e) {
            return; // 已入账，忽略重复通知
        }
        pointAccountMapper.upsertAddBalance(po.getUserId(), points);
    }

    @Override
    public RechargeStatusVO getRechargeStatus(String orderNo) {
        PointsRechargeOrder ro = rechargeOrderMapper.selectByOrderNo(orderNo);
        PayOrder po = payOrderMapper.selectByOrderNo(orderNo);
        if (ro == null || po == null) {
            return null;
        }
        // 惰性关单：用户在线轮询时若已超时，顺手置 EXPIRED，立刻反馈
        if (PayConstants.STATUS_WAIT_PAY.equals(ro.getStatus())
                && ro.getCreateTime() != null
                && ro.getCreateTime().plusMinutes(alipayProperties.getOrderTimeoutMinutes())
                .isBefore(LocalDateTime.now())) {
            expireOrder(orderNo);
            ro.setStatus(PayConstants.STATUS_EXPIRED);
        }

        RechargeStatusVO vo = new RechargeStatusVO();
        vo.setOrderId(orderNo);
        vo.setStatus(ro.getStatus());
        vo.setPoints(ro.getPoints());
        if (po.getQrCode() != null) {
            vo.setQrCodeUrl(QrCodeUtil.toDataUrl(po.getQrCode()));
        }
        return vo;
    }

    /**
     * 取消充值订单：仅 WAIT_PAY 可取消，本地置 CANCELLED 并主动关闭支付宝订单使二维码失效。
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelRechargeOrder(String orderNo, String userId) {
        PointsRechargeOrder ro = rechargeOrderMapper.selectByOrderNo(orderNo);
        PayOrder po = payOrderMapper.selectByOrderNo(orderNo);
        if (ro == null || po == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(ro.getUserId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!PayConstants.STATUS_WAIT_PAY.equals(ro.getStatus())) {
            throw new BusinessException("仅待支付订单可取消");
        }
        LocalDateTime now = LocalDateTime.now();
        ro.setStatus(PayConstants.STATUS_CANCELLED);
        ro.setUpdateTime(now);
        rechargeOrderMapper.updateById(ro);
        po.setStatus(PayConstants.STATUS_CANCELLED);
        po.setUpdateTime(now);
        payOrderMapper.updateById(po);
        // 主动关闭支付宝订单，使二维码立即失效（已关/未付等异常忽略）
        PayChannelService svc = channelMap.get(po.getChannel());
        if (svc != null) {
            try {
                svc.closeOrder(orderNo);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 超时关单：WAIT_PAY 置 EXPIRED（充值单与支付单同步）。
     * 由定时任务或状态查询惰性触发；重复调用安全。
     */
    @Transactional(rollbackFor = Exception.class)
    public void expireOrder(String orderNo) {
        PointsRechargeOrder ro = rechargeOrderMapper.selectByOrderNo(orderNo);
        PayOrder po = payOrderMapper.selectByOrderNo(orderNo);
        if (ro == null || po == null) {
            return;
        }
        if (!PayConstants.STATUS_WAIT_PAY.equals(ro.getStatus())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        ro.setStatus(PayConstants.STATUS_EXPIRED);
        ro.setUpdateTime(now);
        rechargeOrderMapper.updateById(ro);
        po.setStatus(PayConstants.STATUS_EXPIRED);
        po.setUpdateTime(now);
        payOrderMapper.updateById(po);
    }

    private String genOrderNo() {
        return "RC" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
