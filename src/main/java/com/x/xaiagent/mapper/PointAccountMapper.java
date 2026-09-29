package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.xaiagent.entity.PointAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PointAccountMapper extends BaseMapper<PointAccount> {

    /**
     * 加分：行不存在则插入，存在则原子自增（SQL 见 PointAccountMapper.xml）。
     * 单条语句天然并发安全，调用方需保证 delta > 0。
     *
     * @return 影响行数（正常恒为 1）
     */
    int upsertAddBalance(@Param("userId") String userId, @Param("delta") int delta);

    /**
     * 减分：仅当余额充足（balance >= amount）时原子扣减（SQL 见 PointAccountMapper.xml）。
     * 并发下不会扣成负数；账户行不存在时也返回 0。
     *
     * @return 影响行数，0 = 余额不足
     */
    int deductIfEnough(@Param("userId") String userId, @Param("amount") int amount);
}
