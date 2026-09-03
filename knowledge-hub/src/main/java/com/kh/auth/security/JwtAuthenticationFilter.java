package com.kh.auth.security;

import com.kh.auth.util.JwtUtils;
import com.kh.common.security.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * JWT 认证过滤器：从 Authorization: Bearer {token} 解析身份写入 SecurityContext。
 * 解析失败不阻断请求（保持匿名），由授权规则与认证入口点统一处理未认证响应。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // ① 识别 Authorization 头：规范写法 "Bearer {token}"；同时兼容裸 token（如调试时直接粘贴）。
        //    兼容不影响安全——令牌是否有效仍由签名与过期时间校验决定
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.startsWith(BEARER_PREFIX)
                    ? header.substring(BEARER_PREFIX.length())
                    : header;
            // ② 解析并校验 JWT：过期/签名不符/格式错误一律返回 empty，此处保持匿名、不做阻断
            Optional<LoginUser> loginUser = jwtUtils.parseAccessToken(token);
            if (loginUser.isEmpty()) {
                // 带了头但令牌无效：最常见的两种原因是令牌过期(2h)与复制 token 时不完整
                log.warn("Authorization 头已携带但令牌无效/过期，按匿名处理（请尝试重新登录获取 accessToken）");
            }
            // ③ 解析成功：包装为 Authentication 写入 SecurityContext（ThreadLocal，请求结束自动清理）
            loginUser.ifPresent(user -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, List.of());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }
        // ④ 无论是否认证成功都继续过滤链；"拒绝未认证请求"由 SecurityConfig 授权规则统一裁决（未认证 → code 1010）
        chain.doFilter(request, response);
    }
}
