package com.kh.common.result;

import lombok.Data;

/**
 * 统一返回体：所有接口固定返回 HTTP 200，业务结果由 code 表达，
 * 前端拦截器统一按 code 处理（如 1010 触发令牌刷新流程）。
 *
 * <pre>{ "code": 0, "message": "success", "data": {} }</pre>
 */
@Data
public class R<T> {

    /** 业务码，见 {@link ErrorCode} */
    private int code;

    /** 提示信息 */
    private String message;

    /** 业务数据，可为空 */
    private T data;

    public static <T> R<T> ok() {
        return build(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> R<T> ok(T data) {
        return build(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> R<T> ok(String message, T data) {
        return build(ErrorCode.SUCCESS.getCode(), message, data);
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return build(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /** 使用默认错误码，但替换提示语（用于给默认文案补充上下文，如冲突的具体文件名） */
    public static <T> R<T> fail(ErrorCode errorCode, String message) {
        return build(errorCode.getCode(), message, null);
    }

    public static <T> R<T> fail(int code, String message) {
        return build(code, message, null);
    }

    private static <T> R<T> build(int code, String message, T data) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(data);
        return r;
    }
}
