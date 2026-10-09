package com.x.xaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.x.xaiagent.entity.UserSignMonth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserSignMonthMapper extends BaseMapper<UserSignMonth> {

    /**
     * 查询某月所有签到行的位图（仅 sign_bits 列，SQL 见 UserSignMonthMapper.xml）。
     * 日期换算在 Java 侧用算术完成，SQL 不做位运算。
     *
     * @param month 签到月份，格式 yyyy-MM
     * @return 该月所有签到行的 sign_bits 列表
     */
    List<Integer> selectSignBitsByMonth(@Param("month") String month);
}
