package com.x.xaiagent.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理端用户积分分页结果（GET /admin/points/users → { list, total }）
 */
@Data
public class AdminPointPageVO {

    /** 当前页数据 */
    private List<AdminPointUserVO> list;

    /** 总条数 */
    private Long total;
}
