package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.context.UserContext;
import com.x.xaiagent.interceptor.RequireRole;
import com.x.xaiagent.service.PointService;
import com.x.xaiagent.vo.PointRecordVO;
import com.x.xaiagent.vo.PointRecordsVO;
import com.x.xaiagent.vo.PointSummaryVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户积分（流水查询；后续扩展扣减 / 兑换 / 后台调整）
 */
@RestController
@RequestMapping("/user/points")
public class UserPointController {

    /** 明细最多返回条数（前端当前为整列表渲染 + 本地筛选，先给足够大的上限） */
    private static final int RECORDS_LIMIT = 200;

    @Resource
    private PointService pointService;

    /** 积分明细：最近流水倒序（对齐前端 GET /user/points/records → { list: [...] }） */
    @GetMapping("/records")
    @RequireRole({RoleConstants.USER, RoleConstants.ADMIN})
    public R<PointRecordsVO> records() {
        List<PointRecordVO> list = pointService.listRecords(UserContext.getUserId(), RECORDS_LIMIT);
        PointRecordsVO vo = new PointRecordsVO();
        vo.setList(list);
        return R.ok(vo);
    }

    /** 积分汇总：余额 / 累计获得 / 累计消费（对齐前端 GET /user/points/summary） */
    @GetMapping("/summary")
    @RequireRole({RoleConstants.USER, RoleConstants.ADMIN})
    public R<PointSummaryVO> summary() {
        return R.ok(pointService.getSummary(UserContext.getUserId()));
    }
}
