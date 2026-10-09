package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.context.UserContext;
import com.x.xaiagent.dto.PointAdjustDTO;
import com.x.xaiagent.dto.PointRuleDTO;
import com.x.xaiagent.interceptor.RequireRole;
import com.x.xaiagent.service.PointService;
import com.x.xaiagent.vo.AdminPointUserVO;
import com.x.xaiagent.vo.PageVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分管理（仅管理员）：积分规则配置 + 用户积分列表 + 人工调整。
 * 业务逻辑统一在 {@link PointService}（积分域唯一 Service，用户/管理端入口隔离由 Controller 层负责）。
 */
@RestController
@RequestMapping("/admin/points")
public class PointController {

    @Resource
    private PointService pointService;

    /** 查询积分规则（对齐前端 GET /admin/points/rules） */
    @GetMapping("/rules")
    @RequireRole(RoleConstants.ADMIN)
    public R<PointRuleDTO> rules() {
        return R.ok(pointService.getRules());
    }

    /** 保存积分规则（对齐前端 PUT /admin/points/rules） */
    @PutMapping("/rules")
    @RequireRole(RoleConstants.ADMIN)
    public R<Void> updateRules(@RequestBody PointRuleDTO dto) {
        pointService.updateRules(dto, UserContext.getUserId());
        return R.ok();
    }

    /** 用户积分列表（对齐前端 GET /admin/points/users?keyword=&page=&size=，默认 20 条/页） */
    @GetMapping("/users")
    @RequireRole(RoleConstants.ADMIN)
    public R<PageVO<AdminPointUserVO>> users(@RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return R.ok(pointService.pageUsers(keyword, page, size));
    }

    /** 人工调整积分（对齐前端 POST /admin/points/adjust，reason 必填，减分不可扣成负数） */
    @PostMapping("/adjust")
    @RequireRole(RoleConstants.ADMIN)
    public R<Void> adjust(@RequestBody PointAdjustDTO dto) {
        pointService.adjust(dto, UserContext.getUserId());
        return R.ok();
    }
}
