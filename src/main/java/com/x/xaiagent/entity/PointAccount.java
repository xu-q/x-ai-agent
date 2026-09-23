package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分余额：查询加速层，写入只走 {@link com.x.xaiagent.mapper.PointAccountMapper#upsertAddBalance}
 * （原子增减），与流水同库同事务，可随时 sum 流水重算对账。
 */
@Data
@TableName("point_account")
public class PointAccount {

    /** 用户 ID（主键） */
    @TableId(type = IdType.INPUT)
    private String userId;

    /** 当前积分余额 */
    private Integer balance;

    private LocalDateTime updateTime;
}
