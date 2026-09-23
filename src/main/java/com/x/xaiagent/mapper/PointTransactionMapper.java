package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.xaiagent.entity.PointTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointTransactionMapper extends BaseMapper<PointTransaction> {

    /**
     * 累计获得积分（正数流水之和，SQL 见 PointTransactionMapper.xml）。
     */
    Long sumEarnedPoints(@Param("userId") String userId);

    /**
     * 累计消费积分（负数流水取绝对值之和，SQL 见 PointTransactionMapper.xml）。
     */
    Long sumSpentPoints(@Param("userId") String userId);
}
