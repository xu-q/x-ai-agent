package com.x.xaiagent.vo;

import lombok.Data;

import java.util.List;

/**
 * 积分明细列表响应（对齐前端契约：{ list: [...] }）
 */
@Data
public class PointRecordsVO {

    private List<PointRecordVO> list;
}
