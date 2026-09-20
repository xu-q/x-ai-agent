package com.x.xaiagent.globalExceptionHandler;

/**
 * 业务异常：用于可预期的业务失败（如参数非法、业务规则不满足等）。
 * 由 {@link GlobalExceptionHandler} 统一映射为 HTTP 400。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
