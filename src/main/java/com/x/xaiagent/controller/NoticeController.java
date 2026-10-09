package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.context.UserContext;
import com.x.xaiagent.dto.NoticeBatchDTO;
import com.x.xaiagent.dto.NoticeSaveDTO;
import com.x.xaiagent.interceptor.RequireRole;
import com.x.xaiagent.service.NoticeService;
import com.x.xaiagent.vo.NoticeVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息发布（仅管理员）：公告的增删改、发布/撤回、批量操作。
 */
@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /** 公告列表（仅管理员） */
    @GetMapping
    @RequireRole(RoleConstants.ADMIN)
    public R<List<NoticeVO>> list() {
        return R.ok(noticeService.listNotices());
    }

    /** 新建公告（默认草稿，仅管理员） */
    @PostMapping
    @RequireRole(RoleConstants.ADMIN)
    public R<NoticeVO> create(@RequestBody NoticeSaveDTO dto) {
        return R.ok(noticeService.create(dto, UserContext.getUserId()));
    }

    /** 编辑公告（仅管理员） */
    @PutMapping("/{id}")
    @RequireRole(RoleConstants.ADMIN)
    public R<NoticeVO> update(@PathVariable String id, @RequestBody NoticeSaveDTO dto) {
        return R.ok(noticeService.update(id, dto, UserContext.getUserId()));
    }

    /** 发布（幂等，仅管理员） */
    @PostMapping("/{id}/publish")
    @RequireRole(RoleConstants.ADMIN)
    public R<Void> publish(@PathVariable String id) {
        noticeService.publish(id);
        return R.ok();
    }

    /** 撤回（幂等，仅管理员） */
    @PostMapping("/{id}/withdraw")
    @RequireRole(RoleConstants.ADMIN)
    public R<Void> withdraw(@PathVariable String id) {
        noticeService.withdraw(id);
        return R.ok();
    }

    /** 逻辑删除（仅管理员） */
    @DeleteMapping("/{id}")
    @RequireRole(RoleConstants.ADMIN)
    public R<Void> delete(@PathVariable String id) {
        noticeService.delete(id);
        return R.ok();
    }

    /** 批量发布/撤回（幂等，返回实际变更条数） */
    @PostMapping("/batch-status")
    @RequireRole(RoleConstants.ADMIN)
    public R<Integer> batchStatus(@RequestBody NoticeBatchDTO dto) {
        return R.ok(noticeService.batchStatus(dto));
    }

    /** 批量逻辑删除（返回实际删除条数） */
    @PostMapping("/batch-delete")
    @RequireRole(RoleConstants.ADMIN)
    public R<Integer> batchDelete(@RequestBody NoticeBatchDTO dto) {
        return R.ok(noticeService.batchDelete(dto));
    }
}
