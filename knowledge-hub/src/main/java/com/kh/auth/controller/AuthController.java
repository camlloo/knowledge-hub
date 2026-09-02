package com.kh.auth.controller;

import com.kh.auth.dto.LoginRequest;
import com.kh.auth.dto.RefreshRequest;
import com.kh.auth.dto.RegisterRequest;
import com.kh.auth.dto.TokenResponse;
import com.kh.auth.service.AuthService;
import com.kh.common.result.R;
import com.kh.user.dto.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（清单见 docs/02-文件模块接口设计.md）。
 */
@Tag(name = "认证", description = "注册 / 登录 / 令牌刷新 / 退出")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册：校验用户名唯一 → BCrypt 加密入库（新用户默认 10GB 配额）→ 返回用户信息（不自动登录）
     */
    @Operation(summary = "注册")
    @PostMapping("/register")
    public R<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return R.ok(authService.register(request));
    }

    /**
     * 登录：查库验密（BCrypt）→ 签发双令牌 accessToken(JWT,2h,无状态) + refreshToken(随机串,Redis,7d,可撤销)
     * → 返回令牌与用户信息，前端保存并在请求头携带 Authorization: Bearer {accessToken}
     */
    @Operation(summary = "登录", description = "返回 accessToken / refreshToken；支持多设备同时在线，各设备刷新互不影响")
    @PostMapping("/login")
    public R<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    /**
     * 刷新：按 refreshToken 查 Redis 归属 → 轮换（旧值即删，防重放）→ 校验用户状态 → 签发全新令牌对
     * 前端收到 1010 时调用本接口静默续期，无需重新登录
     */
    @Operation(summary = "刷新令牌", description = "轮换机制：旧 refreshToken 立即作废，返回全新令牌对")
    @PostMapping("/refresh")
    public R<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return R.ok(authService.refresh(request));
    }

    /**
     * 退出：删除 Redis 中的 refreshToken 即完成会话撤销；
     * 已签发的 accessToken 为无状态 JWT，等待自然过期（≤2h），风险窗口可接受
     */
    @Operation(summary = "退出登录", description = "撤销 refreshToken（需携带 accessToken）")
    @PostMapping("/logout")
    public R<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return R.ok();
    }
}
