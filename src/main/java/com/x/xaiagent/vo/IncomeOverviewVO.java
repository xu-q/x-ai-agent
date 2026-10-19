package com.x.xaiagent.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收入总览（GET /stats/income/summary?days=7|30）：
 * 一次返回今日收入、累计收入，以及近 days 天每日收入趋势。
 */
@Data
public class IncomeOverviewVO {

    /** 今日收入（已支付订单，paid_time 为今日） */
    private BigDecimal today;

    /** 累计收入（所有已支付订单金额之和） */
    private BigDecimal total;

    /** 近 days 天每日收入（含今日，升序） */
    private List<DayIncomeVO> trend;
}
