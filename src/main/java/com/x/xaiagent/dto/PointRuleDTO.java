package com.x.xaiagent.dto;

import lombok.Data;

/**
 * 积分规则保存入参（PUT /admin/points/rules）
 */
@Data
public class PointRuleDTO {

    /** 签到基础分（必填，0~100） */
    private Integer signInBasePoints;

    /** 连续签到加成积分（必填，0~100） */
    private Integer continuousBonus;
}
