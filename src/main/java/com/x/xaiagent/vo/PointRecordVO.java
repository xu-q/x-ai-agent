package com.x.xaiagent.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.x.xaiagent.constant.PointTransactionType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分明细单条记录（对齐前端契约：GET /user/points/records）
 */
@Data
public class PointRecordVO {

    private String id;

    private PointTransactionType type;

    /** 展示标题（取自流水 remark） */
    private String title;

    /** 积分变动：正=收入，负=支出 */
    private Integer points;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
