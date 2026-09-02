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

    @Operation(summary = "注册")
    @PostMapping("/register")
    public R<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return R.ok(authService.register(request));
    }

    @Operation(summary = "登录", description = "返回 accessToken / refreshToken，同一用户后登录使前一个 refreshToken 失效")
    @PostMapping("/login")
    public R<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @Operation(summary = "刷新令牌", description = "轮换机制：旧 refreshToken 立即作废，返回全新令牌对")
    @PostMapping("/refresh")
    public R<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return R.ok(authService.refresh(request));
    }

    @Operation(summary = "退出登录", description = "撤销 refreshToken（需携带 accessToken）")
    @PostMapping("/logout")
    public R<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return R.ok();
    }
}
