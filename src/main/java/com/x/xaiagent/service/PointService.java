package com.x.xaiagent.service;

import com.x.xaiagent.dto.PointAdjustDTO;
import com.x.xaiagent.dto.PointRuleDTO;
import com.x.xaiagent.vo.AdminPointPageVO;
import com.x.xaiagent.vo.PointRecordVO;
import com.x.xaiagent.vo.PointSummaryVO;

import java.util.List;

/**
 * 积分服务：积分域全部业务逻辑的唯一归属，按业务域而非调用方划分。
 * 用户端 / 管理端的入口隔离由 Controller 层负责：
 *   - UserPointController（/user/points，USER+ADMIN）
 *   - PointController（/admin/points，仅 ADMIN）
 */
public interface PointService {

    // ==================== 用户端（UserPointController） ====================

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

    // ==================== 管理端（PointController） ====================

    /**
     * 查询积分规则（无配置行时返回默认值：基础分 5、加成 1）。
     */
    PointRuleDTO getRules();

    /**
     * 保存积分规则（单行表：无行则插入，有行则更新），记录操作人。
     */
    void updateRules(PointRuleDTO dto, String operatorId);

    /**
     * 用户积分分页列表：余额/累计获得/累计消费/最近变动，keyword 用户名模糊匹配。
     */
    AdminPointPageVO pageUsers(String keyword, int page, int size);

    /**
     * 后台人工调整积分：写 ADMIN_ADJUST 流水 + 原子变更余额（减分不可扣成负数）。
     */
    void adjust(PointAdjustDTO dto, String operatorId);

    // ==================== 公共计算 ====================

    /**
     * 阶梯签到积分：连续第 day 天（从 1 起）可得 base + (day-1)*bonus。
     * 签到发分与"今日/明日积分"展示共用，保证口径一致。
     *
     * @param base  基础分
     * @param bonus 连续签到加成（每多连续一天递增一个 bonus）
     * @param day   连续第几天（1 起）
     * @return 该天签到可得积分
     */
    default int calcSignPoints(int base, int bonus, int day) {
        return base + (day - 1) * bonus;
    }
}
