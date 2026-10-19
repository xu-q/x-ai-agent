package com.x.xaiagent.service;

import com.x.xaiagent.vo.DayIncomeVO;
import com.x.xaiagent.vo.IncomeOverviewVO;
import com.x.xaiagent.vo.UserTrendVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统计服务（仅管理员）。
 */
public interface StatsService {

    /** 用户总数（sys_user 全部账号数，含已删除） */
    Long totalUsers();

    /** 今日签到用户数 */
    Long todaySignCount();

    /**
     * 用户增长趋势：近 days（仅 7 / 20）天每日累计用户数与新增数。
     * 累计与新增均不含已删除用户（deleted=0）。
     */
    List<UserTrendVO> userTrend(int days);

    /** 今日收入（已支付订单，paid_time 为今日） */
    BigDecimal todayIncome();

    /** 累计收入（所有已支付订单金额之和） */
    BigDecimal totalIncome();

    /**
     * 收入总览：一次返回今日收入、累计收入，以及近 days（仅 7/30）天每日收入。
     */
    IncomeOverviewVO incomeOverview(int days);
}
