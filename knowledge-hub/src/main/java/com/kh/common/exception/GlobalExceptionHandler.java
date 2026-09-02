package com.kh.common.exception;

import com.kh.common.result.ErrorCode;
import com.kh.common.result.R;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：所有异常统一转为 R 结构（HTTP 仍为 200，业务码见 ErrorCode）。
 * <p>认证类异常（AuthenticationException/AccessDeniedException）在阶段① 实现 JWT 过滤器时，
 * 由安全过滤器直接写响应，不走此处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：info 级别记录，提示语直接返回给前端 */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        log.info("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /** @RequestBody 上的 @Valid 校验失败（MethodArgumentNotValidException 是其子类） */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .orElse(ErrorCode.PARAM_INVALID.getMessage());
        return R.fail(ErrorCode.PARAM_INVALID, message);
    }

    /** 方法级参数校验（Spring 6.1+ 内建方法校验，如 @RequestParam 上的 @Min） */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public R<Void> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        return R.fail(ErrorCode.PARAM_INVALID);
    }

    /** 单参数校验（@Validated 类上的 @RequestParam/@PathVariable 约束） */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(ErrorCode.PARAM_INVALID.getMessage());
        return R.fail(ErrorCode.PARAM_INVALID, message);
    }

    /** 请求体缺失或 JSON 格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return R.fail(ErrorCode.PARAM_INVALID, "请求体格式错误");
    }

    /** 缺少必填参数 / 参数类型不匹配 */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public R<Void> handleParamMismatch(Exception e) {
        return R.fail(ErrorCode.PARAM_INVALID);
    }

    /** HTTP 方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(ErrorCode.PARAM_INVALID, "请求方法不支持");
    }

    /** 上传大小超出限制（spring.servlet.multipart 上限） */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return R.fail(ErrorCode.UPLOAD_TOO_LARGE);
    }

    /** 请求路径不存在（含无映射的 API 路径） */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNoResourceFound(NoResourceFoundException e) {
        return R.fail(ErrorCode.NOT_FOUND);
    }

    /** 兜底：未预期的系统异常，不向前端暴露堆栈细节 */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(ErrorCode.INTERNAL_ERROR);
    }
}
