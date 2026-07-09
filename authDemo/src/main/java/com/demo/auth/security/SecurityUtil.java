package com.demo.auth.security;

import com.demo.auth.common.enums.ErrorCode;
import com.demo.auth.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户工具类
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        LoginUser loginUser = getCurrentUser();
        return loginUser.getUserId();
    }

    /**
     * 获取当前登录手机号
     */
    public static String getCurrentPhone() {
        LoginUser loginUser = getCurrentUser();
        return loginUser.getPhone();
    }

    /**
     * 获取当前登录用户信息
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return (LoginUser) authentication.getPrincipal();
    }
}
