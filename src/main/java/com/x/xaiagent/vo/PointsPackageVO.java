package com.x.xaiagent.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 积分充值档位（用户端展示）。
 */
@Data
public class PointsPackageVO {

    private String key;
    private String title;
    private BigDecimal price;
    private Integer totalPoints;
    private Integer bonusPoints;
    private String tag;
    private String desc;
    private boolean hot;
    private Integer sort;
}
