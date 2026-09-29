package com.x.xaiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分规则配置（单行表，恒一行）。
 * 规则值二期接入签到逻辑，本期仅提供后台存取。
 */
@Data
@TableName("point_config")
public class PointConfig {

    /** 主键，UUID 字符串（Java 端生成，IdType.INPUT） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 签到基础分，每日首次签到固定获得 */
    private Integer signBasePoints;

    /** 连续签到加成积分（二期接入签到逻辑） */
    private Integer continuousBonus;

    /** 最后操作人用户 ID（sys_user.id，保存规则的管理员） */
    private String operatorId;

    /** 最后修改时间 */
    private LocalDateTime updateTime;
}
