package com.kh.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求。
 */
public record RegisterRequest(

        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^\\w{3,50}$", message = "用户名须为 3-50 位字母、数字或下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度须为 6-64 位")
        String password,

        @Size(max = 50, message = "昵称最长 50 字符")
        String nickname
) {
}
