package com.x.xaiagent.globalExceptionHandler;

/**
 * 业务异常：用于可预期的业务失败（如参数非法、业务规则不满足、鉴权失效等）。
 * 由 {@link GlobalExceptionHandler} 统一映射：code 同时用作响应体 R.code 与 HTTP 状态码（默认 400，鉴权失效传 401）。
 */
public class BusinessException extends RuntimeException {

    /** 状态码（默认 400）：同时用作响应体 R.code 与 HTTP status */
    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
