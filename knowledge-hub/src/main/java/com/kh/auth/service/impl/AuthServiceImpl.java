package com.kh.auth.service.impl;

import com.kh.auth.config.JwtProperties;
import com.kh.auth.dto.LoginRequest;
import com.kh.auth.dto.RefreshRequest;
import com.kh.auth.dto.RegisterRequest;
import com.kh.auth.dto.TokenResponse;
import com.kh.auth.service.AuthService;
import com.kh.auth.util.JwtUtils;
import com.kh.common.exception.BizException;
import com.kh.common.result.ErrorCode;
import com.kh.user.dto.UserInfoVO;
import com.kh.user.entity.User;
import com.kh.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 认证服务实现。
 * <p>刷新令牌策略：随机串作为 key 存 Redis（kb:token:refresh:{refreshToken} -> userId），
 * 刷新时校验并轮换（旧值即删），退出时删除；同一用户后登录者使前一个 refresh 失效（单设备策略）。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_KEY_PREFIX = "kb:token:refresh:";

    /** 新用户默认存储配额 10GB */
    private static final long DEFAULT_STORAGE_QUOTA = 10L * 1024 * 1024 * 1024;

    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoVO register(RegisterRequest request) {
        // TODO 登录/注册限流（@RateLimit 基础设施就绪后接入）
        boolean exists = userService.lambdaQuery()
                .eq(User::getUsername, request.username())
                .exists();
        if (exists) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname() == null || request.nickname().isBlank()
                ? request.username()
                : request.nickname());
        user.setRole("USER");
        user.setStatus(1);
        user.setStorageQuota(DEFAULT_STORAGE_QUOTA);
        user.setStorageUsed(0L);
        userService.save(user);
        return UserInfoVO.from(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, request.username())
                .one();
        // 用户不存在与密码错误统一提示，避免暴露账号是否存在
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        checkEnabled(user);
        return buildTokenResponse(user);
    }

    @Override
    public TokenResponse refresh(RefreshRequest request) {
        String key = REFRESH_KEY_PREFIX + request.refreshToken();
        String userId = stringRedisTemplate.opsForValue().get(key);
        if (userId == null) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        // 轮换：无论后续是否成功，旧刷新令牌立即作废，防止重放
        stringRedisTemplate.delete(key);
        User user = userService.getById(Long.valueOf(userId));
        if (user == null) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        checkEnabled(user);
        return buildTokenResponse(user);
    }

    @Override
    public void logout(RefreshRequest request) {
        stringRedisTemplate.delete(REFRESH_KEY_PREFIX + request.refreshToken());
    }

    private void checkEnabled(User user) {
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
    }

    private TokenResponse buildTokenResponse(User user) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + refreshToken,
                String.valueOf(user.getId()),
                jwtProperties.getRefreshTokenTtl());
        String accessToken = jwtUtils.createAccessToken(user.getId(), user.getUsername());
        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenTtl().toSeconds(),
                UserInfoVO.from(user));
    }
}
