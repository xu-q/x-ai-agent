package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.xaiagent.constant.PointTransactionType;
import com.x.xaiagent.entity.PointAccount;
import com.x.xaiagent.entity.PointTransaction;
import com.x.xaiagent.entity.UserSignMonth;
import com.x.xaiagent.mapper.PointAccountMapper;
import com.x.xaiagent.mapper.PointTransactionMapper;
import com.x.xaiagent.mapper.UserSignMonthMapper;
import com.x.xaiagent.service.UserSignService;
import com.x.xaiagent.vo.SignInfoVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 签到服务（传统写法：查出来 → Java 判断 → 更新）。
 * 并发幂等由 UNIQUE (user_id, biz_no) 与位图主键兜底：
 * 并发重复签时后到的事务插入流水触发唯一冲突、整体回滚，
 * 由 Controller 捕获 DuplicateKeyException 后按"已签到"返回，业务数据不受影响。
 */
@Service
public class UserSignServiceImpl implements UserSignService {

    /** 每次签到固定获得积分 */
    private static final int SIGN_POINTS = 10;

    /** 近期签到记录展示条数（对齐前端契约） */
    private static final int RECENT_LIMIT = 7;

    /** 近期记录最多回溯天数（防止从未签到用户死循环） */
    private static final int RECENT_LOOKBACK_DAYS = 90;

    @Resource
    private UserSignMonthMapper userSignMonthMapper;

    @Resource
    private PointTransactionMapper pointTransactionMapper;

    @Resource
    private PointAccountMapper pointAccountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignInfoVO sign(String userId) {
        LocalDate today = LocalDate.now();
        boolean firstSign = doSign(userId, today);
        if (firstSign) {
            awardPoints(userId, PointTransactionType.SIGN_IN, "SIGN:" + today, SIGN_POINTS, "每日签到");
        }
        return getSignInfo(userId);
    }

    @Override
    public SignInfoVO getSignInfo(String userId) {
        LocalDate today = LocalDate.now();
        Map<YearMonth, Integer> bitsCache = new HashMap<>();
        int currentBits = bitsOfMonth(userId, YearMonth.from(today), bitsCache);

        boolean signedToday = hasBit(currentBits, today.getDayOfMonth());
        int monthDays = Integer.bitCount(currentBits);

        // 连续天数口径：今天已签从今天起算；未签从昨天起算（跨月由懒加载自动衔接）
        LocalDate cursor = signedToday ? today : today.minusDays(1);
        int continuousDays = 0;
        while (hasBit(bitsOfMonth(userId, YearMonth.from(cursor), bitsCache), cursor.getDayOfMonth())) {
            continuousDays++;
            cursor = cursor.minusDays(1);
        }

        // 近期签到日期（含今天），从今天往回收集，最多回溯 RECENT_LOOKBACK_DAYS 天
        List<String> recentDates = new ArrayList<>(RECENT_LIMIT);
        LocalDate recent = today;
        LocalDate earliest = today.minusDays(RECENT_LOOKBACK_DAYS);
        while (recentDates.size() < RECENT_LIMIT && !recent.isBefore(earliest)) {
            if (hasBit(bitsOfMonth(userId, YearMonth.from(recent), bitsCache), recent.getDayOfMonth())) {
                recentDates.add(recent.toString());
            }
            recent = recent.minusDays(1);
        }

        PointAccount account = pointAccountMapper.selectById(userId);

        SignInfoVO vo = new SignInfoVO();
        vo.setSignedToday(signedToday);
        vo.setContinuousDays(continuousDays);
        vo.setMonthDays(monthDays);
        vo.setRecentDates(recentDates);
        vo.setBalance(account != null && account.getBalance() != null ? account.getBalance() : 0);
        return vo;
    }

    /**
     * 签到置位（查当月行 → 判断该天是否已签 → 未签则置位）。
     *
     * @return true=本次为首签（应发积分）；false=今天已签（幂等跳过）
     */
    private boolean doSign(String userId, LocalDate today) {
        String month = monthKey(today);
        UserSignMonth row = findMonthRow(userId, month);

        // 当月首签：还没有这个月的行，直接插入
        if (row == null) {
            UserSignMonth created = new UserSignMonth();
            created.setUserId(userId);
            created.setSignMonth(month);
            created.setSignBits(1 << (today.getDayOfMonth() - 1));
            created.setUpdateTime(LocalDateTime.now());
            userSignMonthMapper.insert(created);
            return true;
        }

        int bits = row.getSignBits() == null ? 0 : row.getSignBits();
        if (hasBit(bits, today.getDayOfMonth())) {
            return false; // 今天已签，幂等跳过
        }

        // 未签：置位后更新
        UserSignMonth update = new UserSignMonth();
        update.setSignBits(bits | (1 << (today.getDayOfMonth() - 1)));
        update.setUpdateTime(LocalDateTime.now());
        userSignMonthMapper.update(update, new LambdaQueryWrapper<UserSignMonth>()
                .eq(UserSignMonth::getUserId, userId)
                .eq(UserSignMonth::getSignMonth, month));
        return true;
    }

    /**
     * 发积分：写流水（append-only）+ 更新余额（查→Java 累加→更新）。
     * 并发重复发分由流水表 UNIQUE (user_id, biz_no) 兜底：后到事务插入失败即整体回滚。
     */
    private void awardPoints(String userId, PointTransactionType type, String bizNo, int points, String remark) {
        PointTransaction tx = new PointTransaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setUserId(userId);
        tx.setPoints(points);
        tx.setType(type);
        tx.setBizNo(bizNo);
        tx.setRemark(remark);
        tx.setCreateTime(LocalDateTime.now());
        pointTransactionMapper.insert(tx);

        PointAccount account = pointAccountMapper.selectOne(
                new LambdaQueryWrapper<PointAccount>().eq(PointAccount::getUserId, userId));
        if (account == null) {
            // 用户第一笔积分：创建余额行
            PointAccount created = new PointAccount();
            created.setUserId(userId);
            created.setBalance(points);
            created.setUpdateTime(LocalDateTime.now());
            pointAccountMapper.insert(created);
            return;
        }

        PointAccount update = new PointAccount();
        update.setBalance(account.getBalance() + points);
        update.setUpdateTime(LocalDateTime.now());
        pointAccountMapper.update(update, new LambdaQueryWrapper<PointAccount>()
                .eq(PointAccount::getUserId, userId));
    }

    /** 判断某月位图中"第 day 天"是否已签 */
    private boolean hasBit(int bits, int day) {
        return (bits & (1 << (day - 1))) != 0;
    }

    /** 月份键，格式 yyyy-MM */
    private String monthKey(LocalDate date) {
        return YearMonth.from(date).toString();
    }

    /** 查某用户的某月位图行，无则返回 null */
    private UserSignMonth findMonthRow(String userId, String month) {
        return userSignMonthMapper.selectOne(
                new LambdaQueryWrapper<UserSignMonth>()
                        .eq(UserSignMonth::getUserId, userId)
                        .eq(UserSignMonth::getSignMonth, month));
    }

    /** 懒加载某月位图并缓存：连续天数 / 近期记录跨月时按需补查，正常最多查到上月一行 */
    private int bitsOfMonth(String userId, YearMonth month, Map<YearMonth, Integer> cache) {
        return cache.computeIfAbsent(month, m -> {
            UserSignMonth row = findMonthRow(userId, m.toString());
            return row == null || row.getSignBits() == null ? 0 : row.getSignBits();
        });
    }
}
