package com.x.xaiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.x.xaiagent.dto.AdminResetPasswordDTO;
import com.x.xaiagent.dto.UserRegisterDTO;
import com.x.xaiagent.entity.User;
import com.x.xaiagent.vo.PageVO;
import com.x.xaiagent.vo.ResetPasswordVO;
import com.x.xaiagent.vo.UserVO;

public interface UserService extends IService<User> {

    /** 自助注册（默认角色 USER） */
    UserVO register(UserRegisterDTO dto);

    /** 登录校验密码，返回用户信息（会话创建由 Controller 用 HttpSession 完成） */
    UserVO login(String username, String password);

    /** 管理员新建用户（可指定角色） */
    UserVO saveUser(UserRegisterDTO dto);

    /** 管理员更新用户（角色 / 手机号 / 状态 / 可选改密） */
    UserVO updateUser(String id, UserRegisterDTO dto);

    /** 管理员禁用 / 启用用户 */
    UserVO disableUser(String id, int status);

    /**
     * 用户分页列表（不含已逻辑删除），page 从 1 起，size 上限 100。
     * username/phone 模糊匹配，role/status 精确匹配；结果按创建时间倒序。
     */
    PageVO<UserVO> listUsers(int page, int size, String username, String phone, String role, String status);

    /** 查询单个用户 */
    UserVO getUser(String id);

    /** 逻辑删除用户 */
    boolean removeUser(String id);

    /** 当前登录用户信息（拦截器已填充 UserContext） */
    UserVO getCurrentUser();

    /** 当前登录用户更新自己的资料（仅限头像 / 手机号，防越权） */
    UserVO updateProfile(UserRegisterDTO dto);

    /** 管理员重置指定用户密码（newPassword 为空则重置为默认密码） */
    ResetPasswordVO resetPassword(String id, AdminResetPasswordDTO dto);
}
