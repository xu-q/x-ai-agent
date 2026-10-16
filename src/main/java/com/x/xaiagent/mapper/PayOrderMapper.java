package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.xaiagent.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {

    /**
     * 按支付单号查询未删除的支付单。
     */
    PayOrder selectByOrderNo(@Param("orderNo") String orderNo);
}
