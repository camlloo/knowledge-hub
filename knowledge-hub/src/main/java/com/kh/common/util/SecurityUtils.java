package com.kh.common.util;

import com.kh.common.exception.BizException;
import com.kh.common.result.ErrorCode;
import com.kh.common.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具：读取 JwtAuthenticationFilter 写入 SecurityContext 的身份。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 未登录时抛出 1010 业务异常 */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new BizException(ErrorCode.UNAUTHORIZED);
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().userId();
    }
}
