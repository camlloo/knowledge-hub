package com.kh.user.controller;

import com.kh.auth.service.AuthService;
import com.kh.common.result.R;
import com.kh.common.util.SecurityUtils;
import com.kh.user.dto.UpdateMeRequest;
import com.kh.user.dto.UpdatePasswordRequest;
import com.kh.user.dto.UserMeVO;
import com.kh.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口（清单见 docs/02 §2，详细设计见 docs/03-user模块设计.md）。
 */
@Tag(name = "用户", description = "个人信息 / 资料修改 / 密码修改")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 个人信息：当前登录用户 → 查库 → 组装资料与存储配额（quota/used/percentage，前端进度条数据源）
     */
    @Operation(summary = "个人信息", description = "含存储配额使用情况")
    @GetMapping("/me")
    public R<UserMeVO> me() {
        return R.ok(userService.getMe(SecurityUtils.getCurrentUserId()));
    }

    /**
     * 修改资料：白名单合并 nickname/email/avatar → 返回最新 UserMeVO；
     * username/role/storageQuota/status 即使传入也被忽略（防提权）
     */
    @Operation(summary = "修改资料", description = "仅支持修改昵称/邮箱/头像，其余字段不可变")
    @PutMapping("/me")
    public R<UserMeVO> updateMe(@Valid @RequestBody UpdateMeRequest request) {
        return R.ok(userService.updateMe(SecurityUtils.getCurrentUserId(), request));
    }

    /**
     * 修改密码：验旧密码 → BCrypt 加密落库 → 撤销该用户全部 refreshToken（全端踢下线）。
     * 前端成功后应清除本地令牌跳登录页；旧 accessToken 残留窗口 ≤2h（与退出登录一致）
     */
    @Operation(summary = "修改密码", description = "成功后所有设备强制下线（refreshToken 全部作废）")
    @PutMapping("/me/password")
    public R<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 薄编排：改库（user 域）与踢会话（auth 域）分属两模块，避免 user → auth 反向依赖
        userService.updatePassword(userId, request);
        authService.revokeAllSessions(userId);
        return R.ok();
    }
}
