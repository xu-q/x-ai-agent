package com.x.xaiagent.controller;

import com.x.xaiagent.comment.JwtTokenProvider;
import com.x.xaiagent.comment.R;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.oss.FileStorageService;
import com.x.xaiagent.oss.OssProperties;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 / 删除（服务端中转至阿里云 OSS）。
 * 受全局 AuthInterceptor 拦截，需登录后带 token 调用。
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OssProperties ossProperties;

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(fileStorageService.upload(file));
    }

    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String userId = resolveUserId(request);
        return R.ok(fileStorageService.uploadAvatar(userId, file));
    }

    /** 从 token 解析当前用户 ID；缺失 / 无效 / 过期统一抛 401 业务异常，由全局处理器返回 */
    private String resolveUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "未登录或 token 缺失");
        }
        try {
            return jwtTokenProvider.parseToken(header.substring(7).trim()).getSubject();
        } catch (JwtException e) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "登录已过期或 token 无效");
        }
    }

    @PostMapping("/delete")
    public R<Void> delete(@RequestParam("key") String key, HttpServletRequest request) {
        String userId = resolveUserId(request);
        // 仅允许删除当前登录用户自己的头像对象，防止越权删除他人文件
        String allowedKey = ossProperties.getBasePath() + "avatar/" + userId;
        if (!allowedKey.equals(key)) {
            return R.fail(HttpStatus.FORBIDDEN.value(), "无权删除该文件");
        }
        fileStorageService.delete(key);
        return R.ok();
    }
}
