package com.kh.auth.dto;

import com.kh.user.dto.UserInfoVO;

/**
 * 令牌响应：登录/刷新成功后返回。
 * expiresIn 为 accessToken 有效期（秒），前端据此安排静默刷新。
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserInfoVO userInfo
) {
}
