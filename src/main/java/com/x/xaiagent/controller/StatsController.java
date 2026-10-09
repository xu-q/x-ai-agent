package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.interceptor.RequireRole;
import com.x.xaiagent.service.StatsService;
import com.x.xaiagent.vo.UserTrendVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计（仅管理员）：用户总数、今日签到数、用户增长趋势。
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Resource
    private StatsService statsService;

    /** 用户总数 */
    @GetMapping("/users/total")
    @RequireRole(RoleConstants.ADMIN)
    public R<Long> totalUsers() {
        return R.ok(statsService.totalUsers());
    }

    /** 今日签到用户数 */
    @GetMapping("/sign/today")
    @RequireRole(RoleConstants.ADMIN)
    public R<Long> todaySignCount() {
        return R.ok(statsService.todaySignCount());
    }

    /** 用户增长趋势：近 7/30 天每日用户总量与新增 */
    @GetMapping("/users/trend")
    @RequireRole(RoleConstants.ADMIN)
    public R<List<UserTrendVO>> userTrend(@RequestParam(defaultValue = "7") int days) {
        return R.ok(statsService.userTrend(days));
    }
}
