package com.x.xaiagent.vo;

import lombok.Data;

import java.util.List;

/**
 * 签到信息（对齐前端 GET /user/sign/info 与 POST /user/sign 响应契约）
 */
@Data
public class SignInfoVO {

    /** 今天是否已签到 */
    private Boolean signedToday;

    /** 连续签到天数（今天未签时从昨天起算） */
    private Integer continuousDays;

    /** 本月签到天数 */
    private Integer monthDays;

    /** 近期签到日期，倒序，格式 yyyy-MM-dd */
    private List<String> recentDates;

    /** 当前积分余额 */
    private Integer balance;
}
