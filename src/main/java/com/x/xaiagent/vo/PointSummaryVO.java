package com.x.xaiagent.vo;

import lombok.Data;

/**
 * 积分汇总（对齐前端契约：GET /user/points/summary → { balance, totalEarned, totalSpent }）
 */
@Data
public class PointSummaryVO {

    /** 当前积分余额 */
    private Integer balance;

    /** 累计获得（正数） */
    private Integer totalEarned;

    /** 累计消费（正数，前端展示时加负号） */
    private Integer totalSpent;
}
