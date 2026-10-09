package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.xaiagent.constant.PointTransactionType;
import com.x.xaiagent.dto.PointAdjustDTO;
import com.x.xaiagent.dto.PointRuleDTO;
import com.x.xaiagent.entity.PointAccount;
import com.x.xaiagent.entity.PointConfig;
import com.x.xaiagent.entity.PointTransaction;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.mapper.PointAccountMapper;
import com.x.xaiagent.mapper.PointRuleConfigMapper;
import com.x.xaiagent.mapper.PointTransactionMapper;
import com.x.xaiagent.service.PointService;
import com.x.xaiagent.vo.AdminPointUserVO;
import com.x.xaiagent.vo.PageVO;
import com.x.xaiagent.vo.PointRecordVO;
import com.x.xaiagent.vo.PointSummaryVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 积分服务实现：用户端查询 + 管理端规则/列表/调整，共六个方法。
 * 余额写入一律走原子 SQL（upsertAddBalance / deductIfEnough），
 * 与流水同库同事务；减分用"条件更新 + 影响行数判断"兜底，并发下不会扣成负数。
 */
@Service
public class PointServiceImpl implements PointService {

    // ==================== 常量 ====================

    /**
     * 规则取值上限（防止误输入天价积分）
     */
    private static final int RULE_MAX = 100;

    /**
     * 列表单页上限（防止恶意大 size 拖垮聚合查询）
     */
    private static final int PAGE_SIZE_MAX = 100;

    // ==================== 依赖 ====================

    @Resource
    private PointTransactionMapper pointTransactionMapper;

    @Resource
    private PointAccountMapper pointAccountMapper;

    @Resource
    private PointRuleConfigMapper pointRuleConfigMapper;

    // ==================== 用户端实现 ====================

    @Override
    public List<PointRecordVO> listRecords(String userId, int limit) {
        Page<PointTransaction> page = pointTransactionMapper.selectPage(
                Page.of(1, limit),
                new LambdaQueryWrapper<PointTransaction>()
                        .eq(PointTransaction::getUserId, userId)
                        .orderByDesc(PointTransaction::getCreateTime));
        return page.getRecords().stream().map(this::toVO).toList();
    }

    @Override
    public PointSummaryVO getSummary(String userId) {
        PointAccount account = pointAccountMapper.selectById(userId);

        PointSummaryVO vo = new PointSummaryVO();
        vo.setBalance(account != null && account.getBalance() != null ? account.getBalance() : 0);
        vo.setTotalEarned(pointTransactionMapper.sumEarnedPoints(userId).intValue());
        vo.setTotalSpent(pointTransactionMapper.sumSpentPoints(userId).intValue());
        return vo;
    }

    // ==================== 管理端实现 ====================

    @Override
    public PointRuleDTO getRules() {
        PointConfig row = selectRuleRow();

        PointRuleDTO dto = new PointRuleDTO();
        if (row != null) {
            dto.setSignInBasePoints(row.getSignBasePoints());
            dto.setContinuousBonus(row.getContinuousBonus());
        } else {
            new BusinessException("积分规则不存在");
        }
        return dto;
    }

    @Override
    public void updateRules(PointRuleDTO dto, String operatorId) {
        validateRule(dto);

        LocalDateTime now = LocalDateTime.now();
        PointConfig row = selectRuleRow();
        if (row == null) {
            // 单行表首建
            PointConfig created = new PointConfig();
            created.setId(UUID.randomUUID().toString());
            created.setSignBasePoints(dto.getSignInBasePoints());
            created.setContinuousBonus(dto.getContinuousBonus());
            created.setOperatorId(operatorId);
            created.setUpdateTime(now);
            pointRuleConfigMapper.insert(created);
            return;
        }

        row.setSignBasePoints(dto.getSignInBasePoints());
        row.setContinuousBonus(dto.getContinuousBonus());
        row.setOperatorId(operatorId);
        row.setUpdateTime(now);
        pointRuleConfigMapper.updateById(row);
    }

    @Override
    public PageVO<AdminPointUserVO> pageUsers(String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), PAGE_SIZE_MAX);

        IPage<AdminPointUserVO> result = pointTransactionMapper.selectUserPointPage(
                Page.of(safePage, safeSize), keyword);

        PageVO<AdminPointUserVO> vo = new PageVO<>();
        vo.setList(result.getRecords());
        vo.setTotal(result.getTotal());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(PointAdjustDTO dto, String operatorId) {
        validateAdjust(dto);

        // 1. 余额变更（原子 SQL，减分不足直接报错回滚）
        if (dto.getPoints() > 0) {
            pointAccountMapper.upsertAddBalance(dto.getUserId(), dto.getPoints());
        } else {
            int rows = pointAccountMapper.deductIfEnough(dto.getUserId(), -dto.getPoints());
            if (rows == 0) {
                throw new BusinessException("余额不足，无法扣减");
            }
        }

        // 2. 写流水（append-only，操作人入审计列）
        PointTransaction tx = new PointTransaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setUserId(dto.getUserId());
        tx.setPoints(dto.getPoints());
        tx.setType(PointTransactionType.ADMIN_ADJUST);
        tx.setBizNo("ADJ:" + UUID.randomUUID());
        tx.setOperatorId(operatorId);
        tx.setRemark(dto.getReason());
        tx.setCreateTime(LocalDateTime.now());
        pointTransactionMapper.insert(tx);
    }

    // ==================== 私有方法 ====================

    /**
     * 流水实体转前端 VO
     */
    private PointRecordVO toVO(PointTransaction tx) {
        PointRecordVO vo = new PointRecordVO();
        vo.setId(tx.getId());
        vo.setType(tx.getType());
        vo.setTitle(tx.getRemark() == null ? "" : tx.getRemark());
        vo.setPoints(tx.getPoints());
        vo.setCreateTime(tx.getCreateTime());
        return vo;
    }

    /**
     * 查规则单行表唯一行（无则 null）
     */
    private PointConfig selectRuleRow() {
        List<PointConfig> rows = pointRuleConfigMapper.selectList(null);
        return rows.get(0);
    }

    /**
     * 规则保存校验：非空且在 0~RULE_MAX 范围内
     */
    private void validateRule(PointRuleDTO dto) {
        if (dto.getSignInBasePoints() == null || dto.getContinuousBonus() == null) {
            throw new BusinessException("签到基础分与连续签到加成不能为空");
        }
        if (dto.getSignInBasePoints() < 0 || dto.getContinuousBonus() < 0
                || dto.getSignInBasePoints() > RULE_MAX || dto.getContinuousBonus() > RULE_MAX) {
            throw new BusinessException("积分规则取值范围 0~" + RULE_MAX);
        }
    }

    /**
     * 积分调整校验：用户/变动/原因必填，变动不能为 0
     */
    private void validateAdjust(PointAdjustDTO dto) {
        if (dto.getUserId() == null || dto.getUserId().isBlank()) {
            throw new BusinessException("用户 ID 不能为空");
        }
        if (dto.getPoints() == null || dto.getPoints() == 0) {
            throw new BusinessException("积分变动不能为 0");
        }
        if (dto.getReason() == null || dto.getReason().isBlank()) {
            throw new BusinessException("原因备注不能为空");
        }
    }
}
