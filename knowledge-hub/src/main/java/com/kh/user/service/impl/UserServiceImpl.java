package com.kh.user.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kh.common.exception.BizException;
import com.kh.common.result.ErrorCode;
import com.kh.user.dto.UpdateMeRequest;
import com.kh.user.dto.UpdatePasswordRequest;
import com.kh.user.dto.UserMeVO;
import com.kh.user.entity.User;
import com.kh.user.mapper.UserMapper;
import com.kh.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserMeVO getMe(Long userId) {
        // ① 主键查库 ② 查不到视为登录态失效（账号被并发删除的极端情况）③ 组装配额视图
        User user = getById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return UserMeVO.of(user);
    }

    @Override
    public UserMeVO updateMe(Long userId, UpdateMeRequest request) {
        // ① 查原记录
        User user = getById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        // ② 白名单式合并：仅 nickname/email/avatar 三个可变字段，null 不覆盖；
        //    username/role/storageQuota/status 天然不在合并范围（防提权）
        if (request.nickname() != null) {
            user.setNickname(request.nickname());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.avatar() != null) {
            user.setAvatar(request.avatar());
        }
        // ③ 落库并返回最新视图（前端免二次请求）
        updateById(user);
        return UserMeVO.of(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        // ① 查库并验证旧密码（错 → 1004；与登录的 1002 区分，语义更精确）
        User user = getById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.OLD_PASSWORD_ERROR);
        }
        // ② 新旧相同拒绝：避免"改了个寂寞"，也防止哈希不变造成审计混淆
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.NEW_PASSWORD_SAME);
        }
        // ③ 只更新密码字段（updateById 非 null 字段策略）；踢会话由 Controller 编排 revokeAllSessions
        User update = new User();
        update.setId(userId);
        update.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        updateById(update);
    }
}
