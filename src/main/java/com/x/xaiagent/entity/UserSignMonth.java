package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户月度签到位图：一人一月一行，sign_bits 第 i 位（bit0 起）表示当月第 i+1 天是否签到。
 * 写入只走 {@link com.x.xaiagent.mapper.UserSignMonthMapper#upsertSign}（幂等 upsert），
 * 不使用 MP 通用 insert/update。
 */
@Data
@TableName("user_sign_month")
public class UserSignMonth {

    /** 用户 ID（联合主键之一，标 @TableId 仅为兼容 MP 操作） */
    @TableId(type = IdType.INPUT)
    private String userId;

    /** 签到月份，格式 yyyy-MM */
    private String signMonth;

    /** 位图：第 i 位 = 当月第 i+1 天是否签到 */
    private Integer signBits;

    private LocalDateTime updateTime;
}
