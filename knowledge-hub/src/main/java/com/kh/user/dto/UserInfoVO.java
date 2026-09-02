package com.kh.user.dto;

import com.kh.user.entity.User;

/**
 * 用户信息视图（注册/登录/个人信息接口复用），不暴露密码哈希。
 */
public record UserInfoVO(
        Long id,
        String username,
        String nickname,
        String email,
        String avatar,
        String role,
        Long storageQuota,
        Long storageUsed
) {

    public static UserInfoVO from(User user) {
        return new UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getStorageQuota(),
                user.getStorageUsed());
    }
}
