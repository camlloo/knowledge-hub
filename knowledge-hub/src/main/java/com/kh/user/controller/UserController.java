package com.kh.user.controller;

import com.kh.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口（阶段①实现，清单见 docs/02-文件模块接口设计.md）。
 * <p>TODO GET  /v1/users/me           个人信息 + 配额使用
 * TODO PUT  /v1/users/me           修改昵称/邮箱/头像
 * TODO PUT  /v1/users/me/password  修改密码（成功后强制重新登录）
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
}
