package com.kh.auth.util;

import com.kh.auth.config.JwtProperties;
import com.kh.common.security.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

/**
 * accessToken 的签发与解析（HS256/HS512，按密钥长度自动选择）。
 * 令牌本身无服务端状态，主动失效依赖 refreshToken 的 Redis 存储与短有效期。
 */
@Slf4j
@Component
public class JwtUtils {

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTokenTtl;

    public JwtUtils(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.issuer = properties.getIssuer();
        this.accessTokenTtl = properties.getAccessTokenTtl();
    }

    public String createAccessToken(Long userId, String username) {
        Date now = new Date();
        // 载荷声明：issuer 签发方 / subject=userId / username 自定义声明 / exp = now + TTL（2h）
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenTtl.toMillis()))
                .signWith(key)
                .compact();
    }

    /** 解析 accessToken；过期/签名不符/格式错误一律返回 empty，不抛出 */
    public Optional<LoginUser> parseAccessToken(String token) {
        try {
            // 验签 + 校验签发方 + 解析载荷；签名不符、过期、格式错误都会抛 JwtException/IllegalArgumentException
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            // 从载荷还原登录身份：subject 即 userId
            return Optional.of(new LoginUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            // 无效令牌不抛出：调用方（JWT 过滤器）按匿名继续处理；这里打印具体原因便于排查"已登录仍 1010"
            log.debug("JWT 解析失败（{}: {}），按匿名处理", e.getClass().getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }
}
