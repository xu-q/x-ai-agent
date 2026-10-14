package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.context.UserContext;
import com.x.xaiagent.globalExceptionHandler.BusinessException;
import com.x.xaiagent.oss.FileStorageService;
import com.x.xaiagent.oss.OssProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传 / 删除（服务端中转至阿里云 OSS）。
 * 受全局 AuthInterceptor 拦截，需登录后带 token 调用（用户信息从 UserContext 取）。
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final OssProperties ossProperties;

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(fileStorageService.upload(file));
    }

    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return R.ok(fileStorageService.uploadAvatar(resolveUserId(), file));
    }

    /** 从拦截器填充的 UserContext 取当前用户 ID；未登录统一抛 401 */
    private String resolveUserId() {
        String userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "未登录或登录已过期");
        }
        return userId;
    }

    @PostMapping("/delete")
    public R<Void> delete(@RequestParam("key") String key) {
        String userId = resolveUserId();
        // 仅允许删除当前登录用户自己的头像对象，防止越权删除他人文件
        String allowedKey = ossProperties.getBasePath() + "avatar/" + userId;
        if (!allowedKey.equals(key)) {
            return R.fail(HttpStatus.FORBIDDEN.value(), "无权删除该文件");
        }
        fileStorageService.delete(key);
        return R.ok();
    }
}
