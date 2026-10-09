package com.x.xaiagent.vo;

import lombok.Data;

/**
 * 按天分组的统计结果（SQL GROUP BY 映射）。
 */
@Data
public class DayCountVO {

    /** 日期，格式 yyyy-MM-dd */
    private String date;

    /** 当日数量 */
    private Long count;
}
