package com.x.xaiagent.vo;

import lombok.Data;

/**
 * 用户增长趋势数据点（GET /stats/users/trend 列表项）。
 */
@Data
public class UserTrendVO {

    /** 日期，格式 yyyy-MM-dd */
    private String date;

    /** 截至当日累计用户数（不含已删除） */
    private Long total;

    /** 当日新增用户数（不含已删除） */
    private Long newCount;
}
