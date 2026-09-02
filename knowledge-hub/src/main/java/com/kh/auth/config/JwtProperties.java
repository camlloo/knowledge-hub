package com.kh.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * JWT 配置（kh.jwt.*，见 application-dev.yml）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kh.jwt")
public class JwtProperties {

    /** HS256 签名密钥，长度须 ≥ 32 字节 */
    private String secret;

    /** accessToken 有效期 */
    private Duration accessTokenTtl = Duration.ofHours(2);

    /** refreshToken 有效期 */
    private Duration refreshTokenTtl = Duration.ofDays(7);

    /** 签发方标识 */
    private String issuer = "knowledge-hub";
}
