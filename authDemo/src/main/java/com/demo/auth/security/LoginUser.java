package com.demo.auth.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前登录用户信息，存于 SecurityContext
 */
@Data
@AllArgsConstructor
public class LoginUser {

    private Long userId;
    private String phone;
}
