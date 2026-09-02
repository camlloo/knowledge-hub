package com.kh.common.exception;

import com.kh.common.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常：service 层遇到业务规则不满足时抛出，
 * 由 GlobalExceptionHandler 统一转为 R.fail。
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务码，见 {@link ErrorCode} */
    private final int code;

    public BizException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    /** 使用指定业务码 + 自定义提示语（如补充冲突的具体名称） */
    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
