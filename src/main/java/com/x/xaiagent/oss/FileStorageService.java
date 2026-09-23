package com.x.xaiagent.oss;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象接口。
 * 上层业务只依赖本接口，与具体存储厂商（阿里云 OSS / MinIO / 本地磁盘等）解耦，
 * 后续接入新存储只需新增一个实现类，无需改动调用方。
 * IO / 存储服务异常由实现类统一包装为 {@link com.x.xaiagent.globalExceptionHandler.SystenException}。
 */
public interface FileStorageService {

    /**
     * 上传文件，返回可访问的 URL（或签名临时 URL）。
     *
     * @param file 待上传的文件
     * @return 文件访问地址
     */
    String upload(MultipartFile file);

    /**
     * 上传用户头像：按 userId 固定 key（无扩展名），重传即覆盖旧图，避免孤儿文件累积。
     *
     * @param userId 当前登录用户 ID（由服务端从 token 解析，前端无法伪造 key）
     * @param file   头像文件（仅允许图片类型）
     * @return 头像访问地址
     */
    String uploadAvatar(String userId, MultipartFile file);

    /**
     * 根据 key 删除已上传的文件。
     *
     * @param key 文件唯一标识（即上传返回 URL 中路径部分）
     */
    void delete(String key);
}
