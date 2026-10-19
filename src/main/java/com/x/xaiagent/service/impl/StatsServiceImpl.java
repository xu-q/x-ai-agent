package com.x.xaiagent.service.impl;

import com.x.xaiagent.constant.PayConstants;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.mapper.PayOrderMapper;
import com.x.xaiagent.mapper.UserMapper;
import com.x.xaiagent.mapper.UserSignMonthMapper;
import com.x.xaiagent.service.StatsService;
import com.x.xaiagent.vo.DayCountVO;
import com.x.xaiagent.vo.DayIncomeVO;
import com.x.xaiagent.vo.IncomeOverviewVO;
import com.x.xaiagent.vo.UserTrendVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserSignMonthMapper userSignMonthMapper;

    @Resource
    private PayOrderMapper payOrderMapper;

    @Override
    public Long totalUsers() {
        // 含已删除：统计全部账号（绕过 @TableLogic 逻辑删除过滤）
        Long count = userMapper.countAll();
        return count == null ? 0L : count;
    }

    @Override
    public Long todaySignCount() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();

        // 幂代替位移：2^(day-1)，day 最大 31，2^30 仍在 int 范围内
        int mask = (int) Math.pow(2, day - 1);

        List<Integer> bitsList = userSignMonthMapper.selectSignBitsByMonth(
                YearMonth.from(today).toString());

        // 除法+取模代替按位与：(sign_bits / mask) % 2 == 1 表示当天已签
        return bitsList.stream()
                .filter(b -> b != null && (b / mask) % 2 == 1)
                .count();
    }

    @Override
    public List<UserTrendVO> userTrend(int days) {
        if (days != 7 && days != 30) {
            throw new BusinessException("仅展示7天或30天数据");
        }
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();

        // 每日新增（不含已删除）
        Map<String, Long> newByDay = userMapper.countNewByDay(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(DayCountVO::getDate, DayCountVO::getCount));

        // 基线：起始日之前的累计注册数（不含已删除）
        Long before = userMapper.countCreatedBefore(startTime);
        long cumulative = before == null ? 0L : before;

        // 逐日生成数据点，空档日期补 0
        List<UserTrendVO> result = new ArrayList<>(days);
        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            String key = d.toString();
            long newCount = newByDay.getOrDefault(key, 0L);
            cumulative += newCount;

            UserTrendVO vo = new UserTrendVO();
            vo.setDate(key);
            vo.setTotal(cumulative);
            vo.setNewCount(newCount);
            result.add(vo);
        }
        return result;
    }

    @Override
    public BigDecimal todayIncome() {
        LocalDate today = LocalDate.now();
        BigDecimal sum = payOrderMapper.sumPaidAmountAfter(
                PayConstants.STATUS_PAID, today.atStartOfDay());
        return sum == null ? BigDecimal.ZERO : sum;
    }

    @Override
    public BigDecimal totalIncome() {
        BigDecimal sum = payOrderMapper.sumPaidAmount(PayConstants.STATUS_PAID);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    @Override
    public IncomeOverviewVO incomeOverview(int days) {
        IncomeOverviewVO vo = new IncomeOverviewVO();
        vo.setToday(todayIncome());
        vo.setTotal(totalIncome());
        vo.setTrend(incomeTrend(days));
        return vo;
    }

    /**
     * 近 days（仅 7 / 30）天每日收入金额。空档日期补 0，按日期升序返回。
     */
    private List<DayIncomeVO> incomeTrend(int days) {
        if (days != 7 && days != 30) {
            throw new BusinessException("仅展示7天或30天数据");
        }
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);
        LocalDateTime startTime = start.atStartOfDay();

        Map<String, BigDecimal> byDay = payOrderMapper.sumPaidAmountByDay(
                        PayConstants.STATUS_PAID, startTime)
                .stream()
                .collect(Collectors.toMap(DayIncomeVO::getDate, DayIncomeVO::getAmount));

        // 逐日生成数据点，空档日期补 0
        List<DayIncomeVO> result = new ArrayList<>(days);
        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            String key = d.toString();
            DayIncomeVO dayVo = new DayIncomeVO();
            dayVo.setDate(key);
            dayVo.setAmount(byDay.getOrDefault(key, BigDecimal.ZERO));
            result.add(dayVo);
        }
        return result;
    }
}
