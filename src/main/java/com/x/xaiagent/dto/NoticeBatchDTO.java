package com.x.xaiagent.dto;

import lombok.Data;

import java.util.List;

/**
 * 公告批量操作入参。
 * batch-status 使用 ids + action（publish / withdraw）；batch-delete 仅使用 ids。
 */
@Data
public class NoticeBatchDTO {

    /** 目标公告 ID 列表（必填） */
    private List<String> ids;

    /** 批量动作：publish / withdraw（仅 batch-status 使用，batch-delete 忽略） */
    private String action;
}
