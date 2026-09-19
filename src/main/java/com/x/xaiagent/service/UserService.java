package com.x.xaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.x.xaiagent.dto.UserRegisterDTO;
import com.x.xaiagent.entity.User;
import com.x.xaiagent.vo.UserVO;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {

    /** 自助注册（默认角色 USER） */
    UserVO register(UserRegisterDTO dto);

    /** 登录校验并签发 JWT，返回 token 与用户信息 */
    Map<String, Object> login(String username, String password);

    /** 管理员新建用户（可指定角色） */
    UserVO saveUser(UserRegisterDTO dto);

    /** 管理员更新用户（角色 / 手机号 / 状态 / 可选改密） */
    UserVO updateUser(String id, UserRegisterDTO dto);

    /** 管理员禁用 / 启用用户 */
    UserVO disableUser(String id, int status);

    /** 用户列表（不含已逻辑删除） */
    List<UserVO> listUsers();

    /** 查询单个用户 */
    UserVO getUser(String id);

    /** 逻辑删除用户 */
    boolean removeUser(String id);
}
