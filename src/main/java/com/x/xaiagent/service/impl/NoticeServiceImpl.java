package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.xaiagent.constant.NoticeStatus;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.dto.NoticeBatchDTO;
import com.x.xaiagent.dto.NoticeSaveDTO;
import com.x.xaiagent.entity.Notice;
import com.x.xaiagent.entity.NoticeRead;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.mapper.NoticeMapper;
import com.x.xaiagent.mapper.NoticeReadMapper;
import com.x.xaiagent.service.NoticeService;
import com.x.xaiagent.vo.NoticeVO;
import com.x.xaiagent.vo.UserNoticeVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 消息发布 / 系统通知服务实现。
 * 状态机：DRAFT → PUBLISHED → WITHDRAWN，可再 PUBLISHED；删除走逻辑删除。
 * publish / withdraw / 已读均为幂等操作，批量接口返回实际变更条数。
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    /** 标题长度上限 */
    private static final int TITLE_MAX = 100;

    /** 可见角色合法取值（对齐 RoleConstants） */
    private static final Set<String> VALID_SCOPES = Set.of(
            RoleConstants.ADMIN, RoleConstants.USER, RoleConstants.GUEST);

    @Resource
    private NoticeMapper noticeMapper;

    @Resource
    private NoticeReadMapper noticeReadMapper;

    // ==================== 管理端 ====================

    @Override
    public List<NoticeVO> listNotices() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                        .orderByDesc(Notice::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public NoticeVO create(NoticeSaveDTO dto, String operatorId) {
        validate(dto);

        Notice n = new Notice();
        n.setId(UUID.randomUUID().toString());
        n.setType(dto.getType());
        n.setTitle(dto.getTitle().trim());
        n.setContent(dto.getContent());
        n.setScope(String.join(",", dto.getScopes()));
        n.setStatus(NoticeStatus.DRAFT); // 新建默认草稿，手动发布
        n.setCreateTime(LocalDateTime.now());
        n.setOperatorId(operatorId);
        noticeMapper.insert(n);
        return toVO(n);
    }

    @Override
    public NoticeVO update(String id, NoticeSaveDTO dto, String operatorId) {
        validate(dto);

        Notice n = getById(id);
        n.setType(dto.getType());
        n.setTitle(dto.getTitle().trim());
        n.setContent(dto.getContent());
        n.setScope(String.join(",", dto.getScopes()));
        n.setUpdateTime(LocalDateTime.now());
        n.setOperatorId(operatorId);
        noticeMapper.updateById(n);
        return toVO(n);
    }

    @Override
    public void publish(String id) {
        Notice n = getById(id);
        if (n.getStatus() == NoticeStatus.PUBLISHED) {
            return; // 幂等：已发布跳过
        }
        n.setStatus(NoticeStatus.PUBLISHED);
        if (n.getPublishTime() == null) {
            n.setPublishTime(LocalDateTime.now()); // 首次发布时间只记一次
        }
        n.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(n);
    }

    @Override
    public void withdraw(String id) {
        Notice n = getById(id);
        if (n.getStatus() != NoticeStatus.PUBLISHED) {
            return; // 幂等：非发布态跳过
        }
        n.setStatus(NoticeStatus.WITHDRAWN);
        n.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(n);
    }

    @Override
    public void delete(String id) {
        getById(id); // 不存在则抛异常
        noticeMapper.deleteById(id); // 逻辑删除
    }

    @Override
    public int batchStatus(NoticeBatchDTO dto) {
        validateBatch(dto);
        boolean publish = "publish".equalsIgnoreCase(dto.getAction());
        if (!publish && !"withdraw".equalsIgnoreCase(dto.getAction())) {
            throw new BusinessException("action 仅支持 publish / withdraw");
        }

        int affected = 0;
        for (String id : dto.getIds()) {
            Notice n = getById(id);
            if (publish && n.getStatus() != NoticeStatus.PUBLISHED) {
                n.setStatus(NoticeStatus.PUBLISHED);
                if (n.getPublishTime() == null) {
                    n.setPublishTime(LocalDateTime.now());
                }
                n.setUpdateTime(LocalDateTime.now());
                noticeMapper.updateById(n);
                affected++;
            } else if (!publish && n.getStatus() == NoticeStatus.PUBLISHED) {
                n.setStatus(NoticeStatus.WITHDRAWN);
                n.setUpdateTime(LocalDateTime.now());
                noticeMapper.updateById(n);
                affected++;
            }
        }
        return affected;
    }

    @Override
    public int batchDelete(NoticeBatchDTO dto) {
        validateBatch(dto);
        int removed = 0;
        for (String id : dto.getIds()) {
            if (noticeMapper.selectById(id) != null) {
                noticeMapper.deleteById(id); // 逻辑删除
                removed++;
            }
        }
        return removed;
    }

    // ==================== 用户端 ====================

    @Override
    public List<UserNoticeVO> listForUser(String userId, String role) {
        List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, NoticeStatus.PUBLISHED)
                .like(Notice::getScope, role)
                .orderByDesc(Notice::getCreateTime));
        if (notices.isEmpty()) {
            return List.of();
        }

        // 已读状态：一次性查出当前用户对这些通知的已读记录，避免逐条查
        List<String> noticeIds = notices.stream().map(Notice::getId).toList();
        Set<String> readIds = noticeReadMapper.selectList(new LambdaQueryWrapper<NoticeRead>()
                        .eq(NoticeRead::getUserId, userId)
                        .in(NoticeRead::getNoticeId, noticeIds))
                .stream().map(NoticeRead::getNoticeId).collect(Collectors.toSet());

        return notices.stream().map(n -> {
            UserNoticeVO vo = new UserNoticeVO();
            vo.setId(n.getId());
            vo.setType(n.getType());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setCreateTime(n.getCreateTime());
            vo.setRead(readIds.contains(n.getId()));
            return vo;
        }).toList();
    }

    @Override
    public void markRead(String userId, String noticeId) {
        Notice n = getById(noticeId);
        if (n.getStatus() != NoticeStatus.PUBLISHED) {
            throw new BusinessException("通知不存在或未发布");
        }
        noticeReadMapper.insertIgnore(UUID.randomUUID().toString(), userId, noticeId);
    }

    @Override
    public void markAllRead(String userId, String role) {
        List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, NoticeStatus.PUBLISHED)
                .like(Notice::getScope, role));
        for (Notice n : notices) {
            noticeReadMapper.insertIgnore(UUID.randomUUID().toString(), userId, n.getId());
        }
    }

    // ==================== 私有方法 ====================

    private Notice getById(String id) {
        Notice n = noticeMapper.selectById(id);
        if (n == null) {
            throw new BusinessException("通知不存在");
        }
        return n;
    }

    private void validate(NoticeSaveDTO dto) {
        if (dto.getType() == null) {
            throw new BusinessException("通知类型不能为空");
        }
        if (dto.getScopes() == null || dto.getScopes().isEmpty()) {
            throw new BusinessException("通知范围不能为空");
        }
        for (String s : dto.getScopes()) {
            if (!VALID_SCOPES.contains(s)) {
                throw new BusinessException("通知范围取值非法");
            }
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BusinessException("标题不能为空");
        }
        if (dto.getTitle().trim().length() > TITLE_MAX) {
            throw new BusinessException("标题长度不能超过 " + TITLE_MAX + " 字符");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new BusinessException("内容不能为空");
        }
    }

    private void validateBatch(NoticeBatchDTO dto) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            throw new BusinessException("ids 不能为空");
        }
    }

    /** 逗号分隔的角色串 → 角色列表（去空白、去空项） */
    private List<String> splitScopes(String scope) {
        if (scope == null || scope.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scope.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private NoticeVO toVO(Notice n) {
        NoticeVO vo = new NoticeVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setScopes(splitScopes(n.getScope()));
        vo.setStatus(n.getStatus());
        vo.setCreateTime(n.getCreateTime());
        vo.setUpdateTime(n.getUpdateTime());
        vo.setPublishTime(n.getPublishTime());
        return vo;
    }
}
