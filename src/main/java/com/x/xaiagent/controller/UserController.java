package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.constant.RoleConstants;
import com.x.xaiagent.dto.UserLoginDTO;
import com.x.xaiagent.dto.UserRegisterDTO;
import com.x.xaiagent.interceptor.RequireRole;
import com.x.xaiagent.service.UserService;
import com.x.xaiagent.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /** 自助注册 */
    @PostMapping("/register")
    public R<UserVO> register(@RequestBody UserRegisterDTO dto) {
        return R.ok(userService.register(dto));
    }

    /** 登录，返回 JWT 与用户信息 */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody UserLoginDTO dto) {
        return R.ok(userService.login(dto.getUsername(), dto.getPassword()));
    }

    /** 退出登录（无状态 JWT，客户端丢弃 token 即可；此处仅作约定占位） */
    @PostMapping("/logout")
    public R<String> logout() {
        return R.ok("已退出登录");
    }

    /** 用户列表（仅管理员） */
    @GetMapping("/list")
    @RequireRole(RoleConstants.ADMIN)
    public R<List<UserVO>> listUsers() {
        return R.ok(userService.listUsers());
    }

    /** 查询单个用户（仅管理员） */
    @GetMapping("/{id}")
    @RequireRole(RoleConstants.ADMIN)
    public R<UserVO> getUser(@PathVariable String id) {
        return R.ok(userService.getUser(id));
    }

    /** 管理员新建用户 */
    @PostMapping("/")
    @RequireRole(RoleConstants.ADMIN)
    public R<UserVO> saveUser(@RequestBody UserRegisterDTO dto) {
        return R.ok(userService.saveUser(dto));
    }

    /** 管理员更新用户 */
    @PutMapping("/{id}")
    @RequireRole(RoleConstants.ADMIN)
    public R<UserVO> updateUser(@PathVariable String id, @RequestBody UserRegisterDTO dto) {
        return R.ok(userService.updateUser(id, dto));
    }

    /** 管理员删除用户（逻辑删除） */
    @DeleteMapping("/{id}")
    @RequireRole(RoleConstants.ADMIN)
    public R<Boolean> removeUser(@PathVariable String id) {
        return R.ok(userService.removeUser(id));
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public R<UserVO> getCurrentUser(HttpServletRequest request) {
        return R.ok(userService.getCurrentUser(request));
    }

    /** 当前登录用户更新自己的资料（仅限头像 / 手机号，防越权） */
    @PutMapping("/profile")
    public R<UserVO> updateProfile(@RequestBody UserRegisterDTO dto, HttpServletRequest request) {
        return R.ok(userService.updateProfile(dto, request));
    }
}
