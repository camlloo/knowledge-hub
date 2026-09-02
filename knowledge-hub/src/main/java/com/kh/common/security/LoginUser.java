package com.kh.common.security;

/**
 * 登录用户身份，JWT 过滤器解析后作为 principal 写入 SecurityContext，
 * 业务层通过 SecurityUtils 获取。
 */
public record LoginUser(Long userId, String username) {
}
