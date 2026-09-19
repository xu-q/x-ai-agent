package com.x.xaiagent.interceptor;

import com.x.xaiagent.component.JwtTokenProvider;
import com.x.xaiagent.context.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 鉴权拦截器：
 * 1. 解析 Authorization: Bearer <token>
 * 2. 校验通过后填充 UserContext
 * 3. 方法标注 @RequireRole 时做角色门禁（无 token → 401，角色不符 → 403）
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        RequireRole requireRole = hm.getMethodAnnotation(RequireRole.class);

        String token = resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            if (requireRole != null) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
                return false;
            }
            // 无角色要求的方法，允许匿名访问
            return true;
        }

        Claims claims = jwtTokenProvider.parseToken(token);
        UserContext.set(claims.getSubject(), claims.get("role", String.class));

        if (requireRole != null) {
            String role = UserContext.getRole();
            boolean allowed = false;
            for (String r : requireRole.value()) {
                if (r.equals(role)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }

    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
