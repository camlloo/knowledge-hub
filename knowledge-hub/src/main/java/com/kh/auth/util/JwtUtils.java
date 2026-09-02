package com.kh.auth.util;

import com.kh.auth.config.JwtProperties;
import com.kh.common.security.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

/**
 * accessToken 的签发与解析（HS256）。
 * 令牌本身无服务端状态，主动失效依赖 refreshToken 的 Redis 存储与短有效期。
 */
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
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new LoginUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
