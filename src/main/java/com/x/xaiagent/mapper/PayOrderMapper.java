package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.xaiagent.entity.PayOrder;
import com.x.xaiagent.vo.DayIncomeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {

    /**
     * 按支付单号查询未删除的支付单。
     */
    PayOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 累计收入：指定状态（通常为已支付 PAID）所有订单金额之和。
     */
    BigDecimal sumPaidAmount(@Param("status") String status);

    /**
     * 指定时间之后的收入（如今日）：status 且 paid_time >= paidAfter。
     */
    BigDecimal sumPaidAmountAfter(@Param("status") String status,
                                  @Param("paidAfter") LocalDateTime paidAfter);

    /**
     * 按天统计收入：status 且 paid_time >= start，按日期分组。
     */
    List<DayIncomeVO> sumPaidAmountByDay(@Param("status") String status,
                                         @Param("start") LocalDateTime start);
}
