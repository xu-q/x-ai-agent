package com.x.xaiagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.x.xaiagent.component.JwtTokenProvider;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.dto.UserRegisterDTO;
import com.x.xaiagent.entity.User;
import com.x.xaiagent.mapper.UserMapper;
import com.x.xaiagent.service.UserService;
import com.x.xaiagent.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    private User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public UserVO register(UserRegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (getByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
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
    public Map<String, Object> login(String username, String password) {
        User u = getByUsername(username);
        if (u == null || u.getStatus() == null || u.getStatus() != 1) {
            throw new IllegalArgumentException("用户不存在或已禁用");
        }
        if (!BCrypt.checkpw(password, u.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        String token = jwtTokenProvider.generateToken(u.getId(), u.getRole());
        Map<String, Object> result = new HashMap<>(2);
        result.put("token", token);
        result.put("user", UserVO.from(u));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO saveUser(UserRegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        if (getByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        u.setPhone(dto.getPhone());
        if (!StringUtils.hasText(dto.getRole())) {
            throw new IllegalArgumentException("新建用户必须指定角色");
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
            throw new IllegalArgumentException("用户不存在");
        }
        if (StringUtils.hasText(dto.getPhone())) {
            u.setPhone(dto.getPhone());
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
            throw new IllegalArgumentException("用户不存在");
        }
        u.setStatus(status);
        u.setUpdateTime(LocalDateTime.now());
        updateById(u);
        return UserVO.from(u);
    }

    @Override
    public List<UserVO> listUsers() {
        return list().stream().map(UserVO::from).collect(Collectors.toList());
    }

    @Override
    public UserVO getUser(String id) {
        User u = getById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return UserVO.from(u);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUser(String id) {
        return removeById(id);
    }

    @Override
    public UserVO getCurrentUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        UserVO user = null;
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            return user = getUser(jwtTokenProvider.parseToken(token).getSubject());

        }
        return user;
    }
}
