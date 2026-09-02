package com.kh.auth.service;

import com.kh.auth.dto.LoginRequest;
import com.kh.auth.dto.RefreshRequest;
import com.kh.auth.dto.RegisterRequest;
import com.kh.auth.dto.TokenResponse;
import com.kh.user.dto.UserInfoVO;

/**
 * 认证服务：注册、登录签发 JWT、令牌刷新轮换、退出撤销（不映射具体表，编排 UserService 与 Redis）。
 */
public interface AuthService {

    UserInfoVO register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    /** 轮换刷新：旧 refreshToken 立即作废，签发全新令牌对 */
    TokenResponse refresh(RefreshRequest request);

    /** 撤销 refreshToken（accessToken 等待自然过期，见接口文档说明） */
    void logout(RefreshRequest request);

    /** 撤销指定用户全部 refreshToken（改密码全端踢下线；已签发 accessToken 残留窗口 ≤2h） */
    void revokeAllSessions(Long userId);
}
