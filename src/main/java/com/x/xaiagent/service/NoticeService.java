package com.x.xaiagent.service;

import com.x.xaiagent.dto.NoticeBatchDTO;
import com.x.xaiagent.dto.NoticeSaveDTO;
import com.x.xaiagent.vo.NoticeVO;
import com.x.xaiagent.vo.UserNoticeVO;

import java.util.List;

/**
 * 消息发布 / 系统通知服务。
 * 管理端入口见 NoticeController（/notices），用户端见 UserNoticeController（/user/notices）。
 */
public interface NoticeService {

    // ==================== 管理端 ====================

    /** 公告列表（未删，按创建时间倒序） */
    List<NoticeVO> listNotices();

    /** 新建公告（默认草稿），记录操作人 */
    NoticeVO create(NoticeSaveDTO dto, String operatorId);

    /** 编辑公告（不限状态），记录操作人 */
    NoticeVO update(String id, NoticeSaveDTO dto, String operatorId);

    /** 发布（幂等：已发布则跳过），首次发布时间只记一次 */
    void publish(String id);

    /** 撤回（幂等：非发布态跳过） */
    void withdraw(String id);

    /** 逻辑删除 */
    void delete(String id);

    /** 批量发布/撤回（幂等），返回实际变更条数 */
    int batchStatus(NoticeBatchDTO dto);

    /** 批量逻辑删除，返回实际删除条数 */
    int batchDelete(NoticeBatchDTO dto);

    // ==================== 用户端 ====================

    /** 当前用户可见的已发布通知（按 role 过滤 scope），含 read 状态 */
    List<UserNoticeVO> listForUser(String userId, String role);

    /** 标记单条已读（幂等） */
    void markRead(String userId, String noticeId);

    /** 全部已读（幂等，仅当前可见的已发布通知） */
    void markAllRead(String userId, String role);
}
