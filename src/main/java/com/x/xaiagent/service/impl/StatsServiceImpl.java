package com.x.xaiagent.service.impl;

import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.mapper.UserMapper;
import com.x.xaiagent.mapper.UserSignMonthMapper;
import com.x.xaiagent.service.StatsService;
import com.x.xaiagent.vo.DayCountVO;
import com.x.xaiagent.vo.UserTrendVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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

    @Override
    public Long totalUsers() {
        // @TableLogic 自动过滤 deleted=1
        Long count = userMapper.selectCount(null);
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
            throw new BusinessException("days 仅支持 7 或 30");
        }
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1L);

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();

        // 每日新增（含已删除）
        Map<String, Long> newByDay = userMapper.countNewByDay(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(DayCountVO::getDate, DayCountVO::getCount));

        // 基线：起始日之前的累计注册数（含已删除）
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
}
