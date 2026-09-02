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

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 认证服务实现。
 * <p>刷新令牌策略：随机串作为 key 存 Redis（kb:token:refresh:{refreshToken} → userId，TTL 7 天），
 * 刷新时校验并轮换（旧值即删，防重放），退出时删除；
 * 各设备持有独立 refreshToken，互不影响（多设备并存），退出仅撤销当前设备会话。
 * <p>会话反向索引：kb:token:user-sessions:{userId} = SET(refreshTokens)，供改密码时按用户全端踢下线。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String REFRESH_KEY_PREFIX = "kb:token:refresh:";

    /** 用户会话反向索引：SET 成员为该用户全部 refreshToken，供 revokeAllSessions 按 userId 全端清理 */
    private static final String SESSION_KEY_PREFIX = "kb:token:user-sessions:";

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
        // ① 用户名唯一性校验（并发兜底靠 user 表 uk_username 唯一索引，撞索引转 5000）
        boolean exists = userService.lambdaQuery()
                .eq(User::getUsername, request.username())
                .exists();
        if (exists) {
            throw new BizException(ErrorCode.USERNAME_EXISTS);
        }
        // ② 构建用户：密码必须 BCrypt 单向加密存储；昵称缺省取用户名；新用户默认 10GB 配额
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
        // ③ 落库
        userService.save(user);
        // ④ 转视图返回（实体含密码哈希，绝不出参）
        return UserInfoVO.from(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        // ① 按用户名查库（uk_username 唯一，至多一条）
        User user = userService.lambdaQuery()
                .eq(User::getUsername, request.username())
                .one();
        // ② 验密：用户不存在与密码错误统一提示，避免暴露账号是否存在（防账号枚举）
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        // ③ 账号状态校验（禁用账号不可登录）
        checkEnabled(user);
        // ④ 签发双令牌并返回
        return buildTokenResponse(user);
    }

    @Override
    public TokenResponse refresh(RefreshRequest request) {
        String key = REFRESH_KEY_PREFIX + request.refreshToken();
        // ① 用 refreshToken（本身即 Redis key）查归属 userId；查不到 = 已过期/已轮换/已退出
        String userId = stringRedisTemplate.opsForValue().get(key);
        if (userId == null) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        // ② 轮换：无论后续是否成功，旧刷新令牌立即作废，防止同一令牌被重放；同步移出会话反向索引
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForSet().remove(SESSION_KEY_PREFIX + userId, request.refreshToken());
        // ③ 用户仍需存在且未禁用（避免被删号/封禁后仍可续期）
        User user = userService.getById(Long.valueOf(userId));
        if (user == null) {
            throw new BizException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        checkEnabled(user);
        // ④ 签发全新令牌对（与旧令牌无任何关联，前端整体替换）
        return buildTokenResponse(user);
    }

    @Override
    public void logout(RefreshRequest request) {
        String key = REFRESH_KEY_PREFIX + request.refreshToken();
        String userId = stringRedisTemplate.opsForValue().get(key);
        // 撤销会话：删掉 Redis 中的 refreshToken 即可；
        // accessToken 为无状态 JWT 无法主动作废，等待自然过期（≤2h），风险窗口可接受
        stringRedisTemplate.delete(key);
        // 同步清理会话反向索引（令牌本已失效时查不到 userId，无需清理）
        if (userId != null) {
            stringRedisTemplate.opsForSet().remove(SESSION_KEY_PREFIX + userId, request.refreshToken());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void revokeAllSessions(Long userId) {
        // 全端踢下线（改密码场景）：删除该用户全部 refreshToken，任何设备都无法再续期；
        // 已签发的 accessToken 最多残留 2h（无状态令牌固有窗口），强一致需求可升级 token 版本号方案（TODO）
        String sessionsKey = SESSION_KEY_PREFIX + userId;
        Set<String> tokens = stringRedisTemplate.opsForSet().members(sessionsKey);
        if (tokens != null && !tokens.isEmpty()) {
            Set<String> keys = tokens.stream()
                    .map(t -> REFRESH_KEY_PREFIX + t)
                    .collect(Collectors.toSet());
            stringRedisTemplate.delete(keys);
        }
        stringRedisTemplate.delete(sessionsKey);
    }

    /** 禁用账号（status=0）统一拒绝，错误码 1003 */
    private void checkEnabled(User user) {
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
    }

    /**
     * 签发令牌对（登录与刷新共用）：
     * accessToken 无状态不落库（2h）；refreshToken 以随机串为 key 存 Redis（value=userId，TTL 7 天）。
     * 多设备策略：每次登录各自持有一个独立 refresh，互不影响；刷新/退出只作用于当前那一个。
     */
    private TokenResponse buildTokenResponse(User user) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(
                REFRESH_KEY_PREFIX + refreshToken,
                String.valueOf(user.getId()),
                jwtProperties.getRefreshTokenTtl());
        // 维护会话反向索引（供改密码全端踢下线）；TTL 随最近一次登录/刷新顺延
        stringRedisTemplate.opsForSet().add(SESSION_KEY_PREFIX + user.getId(), refreshToken);
        stringRedisTemplate.expire(SESSION_KEY_PREFIX + user.getId(), jwtProperties.getRefreshTokenTtl());
        String accessToken = jwtUtils.createAccessToken(user.getId(), user.getUsername());
        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenTtl().toSeconds(),
                UserInfoVO.from(user));
    }
}
