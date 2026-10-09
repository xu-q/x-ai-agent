package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.context.UserContext;
import com.x.xaiagent.interceptor.RequireRole;
import com.x.xaiagent.service.NoticeService;
import com.x.xaiagent.vo.UserNoticeVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端系统通知：查询已发布通知（按角色过滤 scope）、标记已读。
 */
@RestController
@RequestMapping("/user/notices")
public class UserNoticeController {

    @Resource
    private NoticeService noticeService;

    /** 当前用户可见的已发布通知（含 read 状态） */
    @GetMapping
    @RequireRole({RoleConstants.USER, RoleConstants.ADMIN, RoleConstants.GUEST})
    public R<List<UserNoticeVO>> list() {
        return R.ok(noticeService.listForUser(UserContext.getUserId(), UserContext.getRole()));
    }

    /** 标记单条已读（幂等） */
    @PostMapping("/{id}/read")
    @RequireRole({RoleConstants.USER, RoleConstants.ADMIN, RoleConstants.GUEST})
    public R<Void> read(@PathVariable String id) {
        noticeService.markRead(UserContext.getUserId(), id);
        return R.ok();
    }

    /** 全部已读（幂等，仅当前可见的已发布通知） */
    @PostMapping("/read-all")
    @RequireRole({RoleConstants.USER, RoleConstants.ADMIN, RoleConstants.GUEST})
    public R<Void> readAll() {
        noticeService.markAllRead(UserContext.getUserId(), UserContext.getRole());
        return R.ok();
    }
}
