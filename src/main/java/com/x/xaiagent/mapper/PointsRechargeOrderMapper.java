package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.xaiagent.entity.PointsRechargeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointsRechargeOrderMapper extends BaseMapper<PointsRechargeOrder> {

    /**
     * 按业务单号查询未删除的充值单。
     */
    PointsRechargeOrder selectByOrderNo(@Param("orderNo") String orderNo);
}
