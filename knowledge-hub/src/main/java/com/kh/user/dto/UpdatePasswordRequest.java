package com.kh.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求：成功后全端踢下线（见 AuthService.revokeAllSessions）。
 */
public record UpdatePasswordRequest(

        @NotBlank(message = "原密码不能为空")
        String oldPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 64, message = "新密码长度须为 6-64 位")
        String newPassword
) {
}
