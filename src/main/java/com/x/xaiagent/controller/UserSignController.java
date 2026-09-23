package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.context.UserContext;
import com.x.xaiagent.interceptor.RequireRole;
import com.x.xaiagent.service.UserSignService;
import com.x.xaiagent.vo.SignInfoVO;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户签到（位图 + 积分账本）
 */
@RestController
@RequestMapping("/user/sign")
public class UserSignController {

    @Resource
    private UserSignService userSignService;

    /** 查询当前用户签到信息：是否已签 / 连续天数 / 本月天数 / 近期记录 / 积分余额 */
    @GetMapping("/info")
    @RequireRole({RoleConstants.USER, RoleConstants.ADMIN})
    public R<SignInfoVO> signInfo() {
        return R.ok(userSignService.getSignInfo(UserContext.getUserId()));
    }

    /** 今日签到（幂等：重复签/并发重复签均返回当前信息；首签 +10 积分） */
    @PostMapping
    @RequireRole({RoleConstants.USER, RoleConstants.ADMIN})
    public R<SignInfoVO> sign() {
        String userId = UserContext.getUserId();
        try {
            return R.ok(userSignService.sign(userId));
        } catch (DuplicateKeyException e) {
            // 并发重复签：另一请求已完成签到（流水表 UNIQUE (user_id, biz_no) 兜底），
            // 且 PG 事务报错后已整体回滚、不能继续，故在事务外重查一次按幂等成功返回
            return R.ok(userSignService.getSignInfo(userId));
        }
    }
}
