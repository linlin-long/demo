package com.demo.auth.service;

import cn.hutool.core.util.StrUtil;
import com.demo.auth.common.enums.ErrorCode;
import com.demo.auth.common.exception.BusinessException;
import com.demo.auth.mapper.UserMapper;
import com.demo.auth.model.dto.LoginRequest;
import com.demo.auth.model.dto.RefreshTokenRequest;
import com.demo.auth.model.dto.RegisterRequest;
import com.demo.auth.model.entity.User;
import com.demo.auth.model.vo.LoginVO;
import com.demo.auth.security.JwtUtil;
import com.demo.auth.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SmsService smsService;

    /**
     * 发送短信验证码
     */
    public void sendSms(String phone) {
        // 校验手机号格式（防御性校验，Controller层已做@Valid）
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.INVALID_PHONE);
        }
        smsService.sendVerificationCode(phone);
    }

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();
        String smsCode = request.getSmsCode();

        // 1. 校验短信验证码
        smsService.verifyCode(phone, smsCode);

        // 2. 检查手机号是否已注册
        User existingUser = userMapper.selectByPhone(phone);
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // 3. 加密密码并创建用户
        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(StrUtil.isNotBlank(request.getNickname())
                ? request.getNickname()
                : "用户" + phone.substring(phone.length() - 4));
        user.setStatus(0); // 正常

        userMapper.insert(user);

        // 4. 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), phone);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        log.info("用户注册成功: userId={}, phone={}", user.getId(), phone);

        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .phone(phone)
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * 用户登录
     */
    public LoginVO login(LoginRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();

        // 1. 查找用户
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new BusinessException(ErrorCode.PHONE_NOT_REGISTERED);
        }

        // 2. 检查账号状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用");
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 4. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 5. 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), phone);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        log.info("用户登录成功: userId={}, phone={}", user.getId(), phone);

        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .phone(phone)
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    /**
     * 刷新Token
     */
    public LoginVO refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. 校验RefreshToken
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 2. 获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "用户不存在");
        }

        // 3. 签发新的Token对
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        return LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
}
