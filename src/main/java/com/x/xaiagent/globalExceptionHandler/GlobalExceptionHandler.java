package com.x.xaiagent.globalExceptionHandler;

import com.x.xaiagent.comment.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器：
 * - 统一返回 JSON 结构 {@link R}，不再返回裸字符串
 * - 依据异常类型设置正确 HTTP 状态码
 * - 兜底异常打印完整堆栈，避免问题被静默吞掉
 * - 不再单独捕获泛化的 RuntimeException，防止具体异常被错误归类
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 业务异常：HTTP 400，业务码取自异常自身 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(R.fail(e.getCode(), e.getMessage()));
    }

    /** 系统异常：HTTP 500，打印完整堆栈 */
    @ExceptionHandler(SystenException.class)
    public ResponseEntity<R<Void>> handleSystem(SystenException e) {
        log.error("系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.fail(e.getCode(), e.getMessage()));
    }

    /** 参数校验失败（@Valid / @Validated）：HTTP 400，聚合字段级错误 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(R.fail(400, msg));
    }

    /** 非法参数：HTTP 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(R.fail(400, e.getMessage()));
    }

    /** 兜底：未预期异常，HTTP 500，不向客户端泄露内部细节，但必须打印堆栈 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        log.error("未预期异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.fail(500, "服务器内部错误"));
    }
}
