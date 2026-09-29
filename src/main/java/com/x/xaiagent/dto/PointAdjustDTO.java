package com.x.xaiagent.dto;

import lombok.Data;

/**
 * 后台人工调整积分入参（POST /admin/points/adjust）
 */
@Data
public class PointAdjustDTO {

    /** 被调整用户 ID（sys_user.id，必填） */
    private String userId;

    /** 积分变动：正=加分，负=减分（不能为 0；减分不可超过当前余额） */
    private Integer points;

    /** 原因备注（必填，写入流水 remark） */
    private String reason;
}
