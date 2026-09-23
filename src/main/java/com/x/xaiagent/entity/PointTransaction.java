package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.x.xaiagent.constant.PointTransactionType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水：append-only 账本，只增不改。
 * balance 可随时由流水重算，本表是积分的唯一事实源。
 */
@Data
@TableName("point_transaction")
public class PointTransaction {

    /** 主键，UUID 字符串（Java 端生成，IdType.INPUT） */
    @TableId(type = IdType.INPUT)
    private String id;

    private String userId;

    /** 积分变动：正=获得，负=消耗 */
    private Integer points;

    private PointTransactionType type;

    /** 业务幂等号（与 userId 联合唯一），如 SIGN:2026-09-23 */
    private String bizNo;

    private String remark;

    private LocalDateTime createTime;
}
