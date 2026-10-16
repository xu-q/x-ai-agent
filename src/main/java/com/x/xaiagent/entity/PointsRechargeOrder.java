package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分充值业务单：与 pay_order 一对一关联，记录本次充值的金额与积分。
 */
@Data
@TableName("points_recharge_order")
public class PointsRechargeOrder {

    /** 主键，UUID 字符串（Java 端生成，IdType.INPUT） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 业务单号（业务唯一），同时作为 pay_order.biz_no */
    private String orderNo;

    /** 充值用户 ID */
    private String userId;

    /** 档位 key；自定义充值时为 NULL */
    private String packageKey;

    /** 充值金额（元） */
    private BigDecimal amount;

    /** 入账积分（自定义充值 = amount*100，下单时锁定） */
    private Integer points;

    /** 充值状态：WAIT_PAY / PAID / CANCELLED / EXPIRED（与 pay_order 同步） */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 逻辑删除：0 未删，1 已删 */
    @TableLogic
    private Integer deleted;
}
