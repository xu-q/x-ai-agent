package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.x.xaiagent.entity.PointAccount;
import com.x.xaiagent.entity.PointTransaction;
import com.x.xaiagent.mapper.PointAccountMapper;
import com.x.xaiagent.mapper.PointTransactionMapper;
import com.x.xaiagent.service.PointService;
import com.x.xaiagent.vo.PointRecordVO;
import com.x.xaiagent.vo.PointSummaryVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointServiceImpl implements PointService {

    @Resource
    private PointTransactionMapper pointTransactionMapper;

    @Resource
    private PointAccountMapper pointAccountMapper;

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

    private PointRecordVO toVO(PointTransaction tx) {
        PointRecordVO vo = new PointRecordVO();
        vo.setId(tx.getId());
        vo.setType(tx.getType());
        vo.setTitle(tx.getRemark() == null ? "" : tx.getRemark());
        vo.setPoints(tx.getPoints());
        vo.setCreateTime(tx.getCreateTime());
        return vo;
    }
}
