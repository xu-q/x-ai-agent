package com.x.xaiagent.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 按天分组的收入统计结果（SQL GROUP BY 映射）。
 */
@Data
public class DayIncomeVO {

    /** 日期，格式 yyyy-MM-dd */
    private String date;

    /** 当日收入金额（元） */
    private BigDecimal amount;
}
