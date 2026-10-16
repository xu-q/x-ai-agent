package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.context.UserContext;
import com.x.xaiagent.dto.CreateRechargeOrderDTO;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.service.PayService;
import com.x.xaiagent.service.PointsPackageService;
import com.x.xaiagent.vo.PayCreateVO;
import com.x.xaiagent.vo.PointsPackageVO;
import com.x.xaiagent.vo.RechargeStatusVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 积分充值（用户端）。
 * 路径前缀 /points（context-path /api 下即 /api/points）。
 */
@RestController
@RequestMapping("/points")
public class PointsRechargeController {

    @Resource
    private PayService payService;

    @Resource
    private PointsPackageService packageService;

    /** 档位列表 */
    @GetMapping("/packages")
    public R<List<PointsPackageVO>> listPackages() {
        return R.ok(packageService.listPackages());
    }

    /** 创建充值订单：返回支付二维码（data URL） */
    @PostMapping("/order")
    public R<PayCreateVO> createOrder(@RequestBody CreateRechargeOrderDTO dto) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return R.fail(401, "未登录");
        }
        return R.ok(payService.createRechargeOrder(dto, userId));
    }

    /** 轮询充值订单状态 */
    @GetMapping("/order/{orderNo}/status")
    public R<RechargeStatusVO> orderStatus(@PathVariable String orderNo) {
        return R.ok(payService.getRechargeStatus(orderNo));
    }

    /** 取消充值订单（仅待支付可取消，立即使支付宝二维码失效） */
    @PostMapping("/order/{orderNo}/cancel")
    public R<Void> cancelOrder(@PathVariable String orderNo) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return R.fail(401, "未登录");
        }
        payService.cancelRechargeOrder(orderNo, userId);
        return R.ok();
    }
}
