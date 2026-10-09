package com.x.xaiagent.service;

import com.x.xaiagent.vo.UserTrendVO;

import java.util.List;

/**
 * 统计服务（仅管理员）。
 */
public interface StatsService {

    /** 用户总数（sys_user 未删除账号数） */
    Long totalUsers();

    /** 今日签到用户数 */
    Long todaySignCount();

    /**
     * 用户增长趋势：近 days（仅 7 / 30）天每日累计用户数与新增数。
     * 累计与新增均含已删除用户（按注册事实统计）。
     */
    List<UserTrendVO> userTrend(int days);
}
