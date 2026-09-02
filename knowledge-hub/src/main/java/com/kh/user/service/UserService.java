package com.kh.user.service;


import com.baomidou.mybatisplus.spring.service.IService;
import com.kh.user.dto.UpdateMeRequest;
import com.kh.user.dto.UpdatePasswordRequest;
import com.kh.user.dto.UserMeVO;
import com.kh.user.entity.User;

/**
 * 用户服务：个人信息/配额查询、资料维护、密码修改。
 * <p>踢下线（撤销 refreshToken）属 auth 模块职责，由 Controller 编排 AuthService.revokeAllSessions 完成。
 */
public interface UserService extends IService<User> {

    /** 个人信息 + 存储配额（GET /users/me） */
    UserMeVO getMe(Long userId);

    /** 白名单更新昵称/邮箱/头像，返回最新信息（PUT /users/me） */
    UserMeVO updateMe(Long userId, UpdateMeRequest request);

    /** 验证旧密码并更新为新密码（PUT /users/me/password；踢会话由调用方编排 revokeAllSessions） */
    void updatePassword(Long userId, UpdatePasswordRequest request);
}
