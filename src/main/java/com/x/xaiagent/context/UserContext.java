package com.x.xaiagent.context;

/**
 * 当前登录用户上下文（基于 ThreadLocal，请求结束由拦截器清理）
 */
public class UserContext {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    public static void set(String userId, String role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static String getRole() {
        return ROLE.get();
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
