package com.kh.user.dto;

import com.kh.user.entity.User;

/**
 * 个人信息视图（GET/PUT /users/me 出参）：资料 + 存储配额。
 */
public record UserMeVO(
        UserInfoVO user,
        QuotaVO quota
) {

    /** 由实体组装：percentage 保留 1 位小数；quota 为空或 0 时取 0，防除零 */
    public static UserMeVO of(User user) {
        long quota = user.getStorageQuota() == null ? 0L : user.getStorageQuota();
        long used = user.getStorageUsed() == null ? 0L : user.getStorageUsed();
        double percentage = quota > 0
                ? Math.round(used * 1000.0 / quota) / 10.0
                : 0.0;
        return new UserMeVO(UserInfoVO.from(user), new QuotaVO(quota, used, percentage));
    }
}
