package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.xaiagent.constant.PointTransactionType;
import com.x.xaiagent.entity.PointAccount;
import com.x.xaiagent.entity.PointTransaction;
import com.x.xaiagent.entity.UserSignMonth;
import com.x.xaiagent.mapper.PointAccountMapper;
import com.x.xaiagent.mapper.PointTransactionMapper;
import com.x.xaiagent.dto.PointRuleDTO;
import com.x.xaiagent.mapper.UserSignMonthMapper;
import com.x.xaiagent.service.PointService;
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

    @Resource
    private UserSignMonthMapper userSignMonthMapper;

    @Resource
    private PointTransactionMapper pointTransactionMapper;

    @Resource
    private PointAccountMapper pointAccountMapper;

    @Resource
    private PointService pointService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignInfoVO sign(String userId) {
        LocalDate today = LocalDate.now();
        boolean firstSign = doSign(userId, today);
        if (firstSign) {
            // 阶梯发分：复用公共方法 calcSignPoints，第 streak 天得分 = base + (streak-1)*bonus
            PointRuleDTO rule = pointService.getRules();
            int streak = countContinuousDays(userId, today, true);
            int award = pointService.calcSignPoints(rule.getSignInBasePoints(), rule.getContinuousBonus(), streak);
            awardPoints(userId, PointTransactionType.SIGN_IN, "SIGN:" + today, award, "每日签到");
        }
        return getSignInfo(userId);
    }

    /**
     * 组装签到信息（GET /user/sign/info 与 POST /user/sign 的响应）。
     *
     * <p>两个关键口径：
     * <ul>
     *   <li><b>continuousDays（连续天数）</b>：已连续签了几天。今天已签则数到今天，
     *       未签则只数到昨天（今天还没签，不算进连续）。</li>
     *   <li><b>todayStreakDay（今天是连续第几天）</b>：序数，从 1 起。已签时等于 continuousDays；
     *       未签时 = continuousDays + 1（今天补签后接在昨天连续之后，若昨天未签则为 1）。</li>
     * </ul>
     *
     * <p>今日/明日积分按数据库积分配置（阶梯口径）推算，非流水实发：配置一改，前端展示即变。
     * 阶梯公式见 {@link PointService#calcSignPoints(int, int, int)}：第 N 天得分 = base + (N-1)*bonus。
     *
     * @param userId 用户 ID
     * @return 签到信息（含今日积分 todayPoints、明日积分 tomorrowPoints）
     */
    @Override
    public SignInfoVO getSignInfo(String userId) {
        LocalDate today = LocalDate.now();

        // ---- 1. 今日是否已签 / 本月签到天数 ----
        Map<YearMonth, Integer> bitsCache = new HashMap<>();
        int currentBits = bitsOfMonth(userId, YearMonth.from(today), bitsCache);

        boolean signedToday = hasBit(currentBits, today.getDayOfMonth());
        int monthDays = Integer.bitCount(currentBits);

        // ---- 2. 连续签到天数 ----
        // 起算点：今天已签从今天往前数；未签则从昨天往前数（今天未签不算连续，跨月由懒加载自动衔接）
        LocalDate cursor = signedToday ? today : today.minusDays(1);
        int continuousDays = 0;
        while (hasBit(bitsOfMonth(userId, YearMonth.from(cursor), bitsCache), cursor.getDayOfMonth())) {
            continuousDays++;
            cursor = cursor.minusDays(1);
        }

        // ---- 3. 今日积分 / 明日积分（阶梯口径，按积分配置推算） ----
        PointRuleDTO rule = pointService.getRules();
        int base = rule.getSignInBasePoints();
        int bonus = rule.getContinuousBonus();

        // 今天是连续第几天：已签=continuousDays；未签=连续天数+1（昨日未签则为 1，即新起一轮得基础分）
        int todayStreakDay = signedToday ? continuousDays : continuousDays + 1;
        // 今日积分：第 todayStreakDay 天的阶梯得分
        int todayPoints = pointService.calcSignPoints(base, bonus, todayStreakDay);
        // 明日积分：预计明日签到所得（连续第 todayStreakDay+1 天）
        int tomorrowPoints = pointService.calcSignPoints(base, bonus, todayStreakDay + 1);

        // ---- 4. 当月签到日期（逐日回填，复用已查出的 currentBits，无需额外查询） ----
        List<String> recentDates = new ArrayList<>();
        for (int day = 1; day <= today.lengthOfMonth(); day++) {
            if (hasBit(currentBits, day)) {
                recentDates.add(today.withDayOfMonth(day).toString());
            }
        }

        // ---- 5. 当前积分余额 ----
        PointAccount account = pointAccountMapper.selectById(userId);

        // ---- 6. 组装响应 ----
        SignInfoVO vo = new SignInfoVO();
        vo.setSignedToday(signedToday);
        vo.setContinuousDays(continuousDays);
        vo.setMonthDays(monthDays);
        vo.setRecentDates(recentDates);
        vo.setTodayPoints(todayPoints);
        vo.setTomorrowPoints(tomorrowPoints);
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

    /**
     * 统计以 endDay 为终点向前连续签到的天数（跨月由懒加载自动衔接）。
     *
     * @param includeEndDay true=终点当天计入统计（签到后算本轮天数）；false=从 endDay 前一天起算
     */
    private int countContinuousDays(String userId, LocalDate endDay, boolean includeEndDay) {
        Map<YearMonth, Integer> bitsCache = new HashMap<>();
        LocalDate cursor = includeEndDay ? endDay : endDay.minusDays(1);
        int days = 0;
        while (hasBit(bitsOfMonth(userId, YearMonth.from(cursor), bitsCache), cursor.getDayOfMonth())) {
            days++;
            cursor = cursor.minusDays(1);
        }
        return days;
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
