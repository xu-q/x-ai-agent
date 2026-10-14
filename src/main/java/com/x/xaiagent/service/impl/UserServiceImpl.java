package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.context.UserContext;
import com.x.xaiagent.dto.UserRegisterDTO;
import com.x.xaiagent.entity.User;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.mapper.UserMapper;
import com.x.xaiagent.service.UserService;
import com.x.xaiagent.vo.PageVO;
import com.x.xaiagent.vo.UserVO;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public UserVO register(UserRegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        if (getByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        u.setPhone(dto.getPhone());
        u.setRole(StringUtils.hasText(dto.getRole()) ? dto.getRole() : RoleConstants.USER);
        u.setStatus(1);
        u.setDeleted(0);
        u.setCreateTime(LocalDateTime.now());
        u.setUpdateTime(LocalDateTime.now());
        if (u.getId() == null) {
            u.setId(UUID.randomUUID().toString());
        }
        save(u);
        return UserVO.from(u);
    }

    @Override
    public UserVO login(String username, String password) {
        User u = getByUsername(username);
        if (u == null || u.getStatus() == null || u.getStatus() != 1) {
            throw new BusinessException("用户不存在或已禁用");
        }
        if (!BCrypt.checkpw(password, u.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        // 会话创建由 Controller 用 HttpSession 完成；此处只返回用户信息
        return UserVO.from(u);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO saveUser(UserRegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        if (getByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        u.setPhone(dto.getPhone());
        if (!StringUtils.hasText(dto.getRole())) {
            throw new BusinessException("新建用户必须指定角色");
        }
        u.setRole(dto.getRole());
        u.setStatus(1);
        u.setDeleted(0);
        u.setCreateTime(LocalDateTime.now());
        u.setUpdateTime(LocalDateTime.now());
        if (u.getId() == null) {
            u.setId(UUID.randomUUID().toString());
        }
        save(u);
        return UserVO.from(u);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(String id, UserRegisterDTO dto) {
        User u = getById(id);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(dto.getPhone())) {
            u.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            u.setAvatar(dto.getAvatar());
        }
        if (StringUtils.hasText(dto.getRole())) {
            u.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            u.setStatus(dto.getStatus());
        }
        if (StringUtils.hasText(dto.getPassword())) {
            u.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        }
        u.setUpdateTime(LocalDateTime.now());
        updateById(u);
        return UserVO.from(u);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO disableUser(String id, int status) {
        User u = getById(id);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        u.setStatus(status);
        u.setUpdateTime(LocalDateTime.now());
        updateById(u);
        return UserVO.from(u);
    }

    @Override
    public PageVO<UserVO> listUsers(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<User> pageQuery = new Page<>(safePage, safeSize);
        page(pageQuery, new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));

        PageVO<UserVO> vo = new PageVO<>();
        vo.setList(pageQuery.getRecords().stream().map(UserVO::from).toList());
        vo.setTotal(pageQuery.getTotal());
        return vo;
    }

    @Override
    public UserVO getUser(String id) {
        User u = getById(id);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.from(u);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUser(String id) {
        return removeById(id);
    }

    @Override
    public UserVO getCurrentUser() {
        // 拦截器已校验会话并填充 UserContext；未登录返回 null
        String userId = UserContext.getUserId();
        if (userId == null) {
            return null;
        }
        User u = getById(userId);
        return u == null ? null : UserVO.from(u);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(UserRegisterDTO dto) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "未登录或登录已过期");
        }
        User u = getById(userId);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        // 仅允许更新头像与手机号，角色 / 状态 / 密码不可通过此接口修改
        if (StringUtils.hasText(dto.getAvatar())) {
            u.setAvatar(dto.getAvatar());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            u.setPhone(dto.getPhone());
        }
        u.setUpdateTime(LocalDateTime.now());
        updateById(u);
        return UserVO.from(u);
    }
}
