package com.x.xaiagent.oss;

import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

/**
 * 阿里云 OSS 实现的 {@link FileStorageService}。
 * 由 Spring 注入 OSS 客户端与配置，对外提供 upload / delete 实例方法，
 * 相比原静态 {@code OssUtil}：依赖显式、可测试、可替换、可享 AOP 能力。
 */
@Slf4j
@Service
public class AliyunOssFileStorageServiceImpl implements FileStorageService, DisposableBean {

    private final OSS ossClient;
    private final OssProperties props;

    public AliyunOssFileStorageServiceImpl(OSS ossClient, OssProperties props) {
        this.ossClient = ossClient;
        this.props = props;
    }

    private static final long MAX_SIZE = 10L * 1024 * 1024;

    // 危险/可执行后缀黑名单，避免上传恶意脚本
    private static final Set<String> BLOCKED_EXT = Set.of(
            "exe", "bat", "sh", "cmd", "php", "jsp", "jspx", "asp", "aspx", "jar", "war");

    // 头像允许的图片后缀白名单
    private static final Set<String> AVATAR_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    @Override
    public String upload(MultipartFile file) throws IOException {
        checkCommon(file);

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.')).toLowerCase() : "";

        String key = props.getBasePath()
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "/" + IdUtil.fastUUID() + ext;

        ObjectMetadata meta = new ObjectMetadata();
        if (file.getContentType() != null) {
            meta.setContentType(file.getContentType());
        }
        meta.setContentLength(file.getSize());

        try (InputStream in = file.getInputStream()) {
            ossClient.putObject(props.getBucketName(), key, in, meta);
        }
        return buildAccessUrl(key);
    }

    @Override
    public String uploadAvatar(String userId, MultipartFile file) throws IOException {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("用户未登录");
        }
        checkCommon(file);
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!AVATAR_EXT.contains(ext)) {
            throw new IllegalArgumentException("头像仅支持 jpg/jpeg/png/gif/webp/bmp");
        }

        // 固定 key：每个用户 OSS 上恒定只有一个头像对象，重传即覆盖旧图，杜绝孤儿文件
        String key = props.getBasePath() + "avatar/" + userId;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(file.getContentType());
        meta.setContentLength(file.getSize());

        try (InputStream in = file.getInputStream()) {
            ossClient.putObject(props.getBucketName(), key, in, meta);
        }
        return buildAccessUrl(key);
    }

    private void checkCommon(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("文件超过上限 10MB");
        }
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.')).toLowerCase() : "";
        if (BLOCKED_EXT.contains(ext.replaceFirst("^\\.", ""))) {
            throw new IllegalArgumentException("不允许的文件类型：" + ext);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key 为空");
        }
        ossClient.deleteObject(props.getBucketName(), key);
    }

    private String buildAccessUrl(String key) {
        if (Boolean.FALSE.equals(props.getIsPrivate())) {
            // 公共读：直链
            if (props.getDomain() != null && !props.getDomain().isBlank()) {
                return props.getDomain().replaceAll("/+$", "") + "/" + key;
            }
            String ep = props.getEndpoint().replaceAll("^https?://", "");
            return "https://" + props.getBucketName() + "." + ep + "/" + key;
        }
        // 私有读：返回带签名的临时 URL
        Date expiration = new Date(System.currentTimeMillis()
                + props.getUrlExpirationMinutes() * 60L * 1000L);
        URL url = ossClient.generatePresignedUrl(props.getBucketName(), key, expiration);
        return url.toString();
    }

    @Override
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
