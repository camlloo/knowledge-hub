package com.kh.common.result;

import lombok.Getter;

/**
 * 全局错误码。
 * <p>分段约定：
 * <ul>
 *   <li>0        成功</li>
 *   <li>1xxx     认证/权限</li>
 *   <li>2xxx     文件域业务（文件/文件夹/标签/分类）</li>
 *   <li>3001     存储配额</li>
 *   <li>4xxx     参数与请求</li>
 *   <li>5000     系统内部错误</li>
 * </ul>
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "success"),

    /* ---------- 认证/权限 1xxx ---------- */
    USERNAME_EXISTS(1001, "用户名已存在"),
    LOGIN_FAILED(1002, "用户名或密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    UNAUTHORIZED(1010, "未登录或登录已过期"),
    REFRESH_TOKEN_INVALID(1011, "登录已失效，请重新登录"),
    FORBIDDEN(1020, "无权访问该资源"),
    RATE_LIMITED(1030, "请求过于频繁，请稍后再试"),

    /* ---------- 文件域 2xxx ---------- */
    FILE_NOT_FOUND(2001, "文件不存在"),
    FOLDER_NOT_FOUND(2002, "文件夹不存在"),
    NAME_CONFLICT(2003, "同级已存在同名项"),
    FILE_TYPE_NOT_ALLOWED(2004, "该文件类型不允许上传"),
    FOLDER_MOVE_CYCLE(2005, "不能将文件夹移动到其自身或子目录下"),

    /* ---------- 配额 3xxx ---------- */
    STORAGE_QUOTA_EXCEEDED(3001, "存储配额不足"),

    /* ---------- 参数/请求 4xxx ---------- */
    PARAM_INVALID(4001, "请求参数不合法"),
    UPLOAD_TOO_LARGE(4002, "上传文件过大"),

    /* ---------- 系统 5xxx ---------- */
    INTERNAL_ERROR(5000, "系统繁忙，请稍后再试"),

    /* ---------- 通用 HTTP 语义 ---------- */
    NOT_FOUND(404, "请求的资源不存在");

    /** 业务码 */
    private final int code;

    /** 默认提示语 */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
