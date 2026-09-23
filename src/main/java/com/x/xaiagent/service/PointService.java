package com.x.xaiagent.service;

import com.x.xaiagent.vo.PointRecordVO;
import com.x.xaiagent.vo.PointSummaryVO;

import java.util.List;

/**
 * 积分服务（流水查询；后续扩展扣减 / 兑换 / 后台调整）
 */
public interface PointService {

    /**
     * 查询用户积分明细，按时间倒序，最多返回 limit 条。
     *
     * @param userId 用户 ID
     * @param limit  最大条数
     * @return 流水列表
     */
    List<PointRecordVO> listRecords(String userId, int limit);

    /**
     * 查询用户积分汇总：余额 / 累计获得 / 累计消费。
     *
     * @param userId 用户 ID
     * @return 汇总信息
     */
    PointSummaryVO getSummary(String userId);
}
