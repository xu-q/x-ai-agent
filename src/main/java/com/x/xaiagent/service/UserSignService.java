package com.x.xaiagent.service;

import com.x.xaiagent.vo.SignInfoVO;

/**
 * 用户签到服务（签到位图 + 积分账本）
 */
public interface UserSignService {

    /**
     * 今日签到（幂等）：首签写流水并加积分，重复签直接返回当前信息。
     *
     * @param userId 当前登录用户 ID
     * @return 签到后最新签到信息（含积分余额）
     */
    SignInfoVO sign(String userId);

    /**
     * 查询签到信息：今日是否已签 / 连续天数 / 本月天数 / 近期记录 / 积分余额。
     *
     * @param userId 当前登录用户 ID
     * @return 签到信息
     */
    SignInfoVO getSignInfo(String userId);
}
