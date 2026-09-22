package com.x.xaiagent.controller;

import com.x.xaiagent.comment.R;
import com.x.xaiagent.oss.FileStorageService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            return R.ok(fileStorageService.upload(file));
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        } catch (Exception e) {
            return R.fail(500, "上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public R<Void> delete(@RequestParam("key") String key) {
        try {
            fileStorageService.delete(key);
            return R.ok();
        } catch (IllegalArgumentException e) {
            return R.fail(400, e.getMessage());
        } catch (Exception e) {
            return R.fail(500, "删除失败：" + e.getMessage());
        }
    }
}
