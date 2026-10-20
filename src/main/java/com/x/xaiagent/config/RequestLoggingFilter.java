package com.x.xaiagent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 请求日志过滤器：在控制台打印每个进入后端的 HTTP 请求（方法、URI、响应码、耗时）。
 * 用于联调时确认前端请求是否真正到达后端，以及接口耗时/是否异常。
 * 优先级最高（最先执行、最后收尾），能覆盖被拦截器拒绝的请求。
 */
@Component
public class RequestLoggingFilter implements jakarta.servlet.Filter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
                         jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String method = req.getMethod();
        String uri = req.getRequestURI();
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("REQUEST {} {} -> {} ({}ms)", method, uri, res.getStatus(), cost);
        }
    }

    @Override
    public int getOrder() {
        // 早于 Spring 自身的 RequestContextFilter / 拦截器，确保任何请求都被记录
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
