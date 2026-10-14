package com.x.xaiagent.interceptor;

import com.x.xaiagent.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 鉴权拦截器（Spring Session 版）：
 * 1. 从 HttpSession（Redis 存储，由 SessionRepositoryFilter 解析 Authorization: Bearer）读取 userId/role
 * 2. 填充 UserContext
 * 3. 方法标注 @RequireRole 时做角色门禁（未登录 → 401，角色不符 → 403）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        RequireRole requireRole = hm.getMethodAnnotation(RequireRole.class);

        HttpSession session = request.getSession(false);
        if (session != null) {
            String userId = (String) session.getAttribute("userId");
            if (userId != null) {
                UserContext.set(userId, (String) session.getAttribute("role"));
            }
        }

        if (requireRole != null) {
            String role = UserContext.getRole();
            if (role == null) {
                writeJson(response, HttpStatus.UNAUTHORIZED.value(), "未登录或登录已过期");
                return false;
            }
            boolean allowed = false;
            for (String r : requireRole.value()) {
                if (r.equals(role)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                writeJson(response, HttpStatus.FORBIDDEN.value(), "无权限访问");
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
