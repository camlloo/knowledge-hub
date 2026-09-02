package com.kh.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 修改资料请求：三字段均可选，传 null 不更新；
 * username/role/storageQuota/status 不在此列——即使前端传了也在白名单更新策略之外（防提权）。
 */
public record UpdateMeRequest(

        @Size(max = 50, message = "昵称最长 50 字符")
        String nickname,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱最长 100 字符")
        String email,

        @Size(max = 255, message = "头像地址最长 255 字符")
        String avatar
) {
}
