package com.x.xaiagent.globalExceptionHandler;

/**
 * 系统异常：用于不可预期的服务端错误（如第三方调用失败、内部状态异常等）。
 * 由 {@link GlobalExceptionHandler} 统一映射为 HTTP 500。
 */
public class SystenException extends RuntimeException {

    public SystenException(String message) {
        super(message);
    }
}
