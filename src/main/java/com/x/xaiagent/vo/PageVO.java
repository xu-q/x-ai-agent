package com.x.xaiagent.vo;

import lombok.Data;

import java.util.List;

/**
 * 通用分页结果（list + total），各模块分页接口复用。
 */
@Data
public class PageVO<T> {

    /** 当前页数据 */
    private List<T> list;

    /** 总条数 */
    private Long total;
}
