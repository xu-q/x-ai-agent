package com.x.xaiagent.globalExceptionHandler;

/**
 * 系统异常：用于不可预期的服务端错误（如第三方调用失败、内部状态异常等）。
 * 由 {@link GlobalExceptionHandler} 统一映射为 HTTP 500。
 */
public class SystenException extends RuntimeException {

    private final int code;

    public SystenException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
