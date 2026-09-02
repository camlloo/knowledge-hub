package com.kh.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新/退出请求，仅携带 refreshToken。
 */
public record RefreshRequest(

        @NotBlank(message = "refreshToken 不能为空")
        String refreshToken
) {
}
