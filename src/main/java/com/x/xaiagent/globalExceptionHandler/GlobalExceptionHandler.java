package com.x.xaiagent.globalExceptionHandler;

import com.x.xaiagent.comment.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器：
 * - 统一返回 JSON 结构 {@link R}，不再返回裸字符串
 * - 业务异常走 {@link BusinessException}（HTTP 码由异常自身 status 决定，默认 400），
 *   系统异常走 {@link SystenException}（HTTP 500），业务代码不再自行 try-catch 拼 R
 * - 框架级异常（参数校验、上传超限）单独映射，兜底异常打印完整堆栈避免被静默吞掉
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 业务异常：code 同时用作 HTTP 状态码（默认 400，鉴权失效为 401）与响应体业务码 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode())
                .body(R.fail(e.getCode(), e.getMessage()));
    }

    /** 系统异常：HTTP 500，打印完整堆栈 */
    @ExceptionHandler(SystenException.class)
    public ResponseEntity<R<Void>> handleSystem(SystenException e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    /** 参数校验失败（@Valid / @Validated）：HTTP 400，聚合字段级错误 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(R.fail(HttpStatus.BAD_REQUEST.value(), msg));
    }

    /** 上传文件超出 multipart 大小限制（容器层在进入 Controller 前拦截）：HTTP 400 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<R<Void>> handleMaxUpload(MaxUploadSizeExceededException e) {
        log.warn("上传文件超出大小限制: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(R.fail(HttpStatus.BAD_REQUEST.value(), "上传文件过大，超出允许的大小限制"));
    }

    /** 兜底：未预期异常，HTTP 500，不向客户端泄露内部细节，但必须打印堆栈 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        log.error("未预期异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误"));
    }
}
