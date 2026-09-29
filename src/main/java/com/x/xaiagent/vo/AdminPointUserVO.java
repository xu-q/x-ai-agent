package com.x.xaiagent.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户积分行（GET /admin/points/users 列表项）
 */
@Data
public class AdminPointUserVO {

    /** 用户 ID（sys_user.id） */
    private String userId;

    /** 用户名 */
    private String username;

    /** 当前积分余额（无余额行时为 0） */
    private Integer balance;

    /** 累计获得（正数流水之和） */
    private Integer totalEarned;

    /** 累计消费（负数流水绝对值之和） */
    private Integer totalSpent;

    /** 最近变动时间（无任何流水时为 null，前端展示为空） */
    private LocalDateTime lastChangeTime;
}
