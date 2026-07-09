package com.demo.auth.controller;

import com.demo.auth.common.ApiResponse;
import com.demo.auth.model.dto.LoginRequest;
import com.demo.auth.model.dto.RefreshTokenRequest;
import com.demo.auth.model.dto.RegisterRequest;
import com.demo.auth.model.dto.SendSmsRequest;
import com.demo.auth.model.vo.LoginVO;
import com.demo.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送短信验证码
     * POST /api/auth/send-sms
     */
    @PostMapping("/send-sms")
    public ApiResponse<Void> sendSms(@Valid @RequestBody SendSmsRequest request) {
        authService.sendSms(request.getPhone());
        return ApiResponse.success("验证码已发送",null);
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        LoginVO vo = authService.register(request);
        return ApiResponse.success("注册成功", vo);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO vo = authService.login(request);
        return ApiResponse.success("登录成功", vo);
    }

    /**
     * 刷新Token
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ApiResponse<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginVO vo = authService.refreshToken(request);
        return ApiResponse.success("Token刷新成功", vo);
    }

    /**
     * 获取当前用户信息（需要登录）
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ApiResponse<LoginVO> getCurrentUser() {
        // 从 SecurityContext 中获取当前用户
        com.demo.auth.security.LoginUser loginUser =
                com.demo.auth.security.SecurityUtil.getCurrentUser();
        // 返回基本信息（实际项目中可查询完整信息返回）
        LoginVO vo = LoginVO.builder()
                .userId(loginUser.getUserId())
                .phone(loginUser.getPhone())
                .build();
        return ApiResponse.success(vo);
    }
}
