package com.demo.auth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 认证相关 1001-1099
    PHONE_ALREADY_REGISTERED(1001, "该手机号已注册"),
    PHONE_NOT_REGISTERED(1002, "该手机号未注册"),
    INVALID_PHONE(1003, "手机号格式不正确"),
    PASSWORD_WEAK(1004, "密码强度不足"),
    PASSWORD_ERROR(1005, "密码错误"),
    INVALID_TOKEN(1006, "无效的Token"),
    TOKEN_EXPIRED(1007, "Token已过期"),
    REFRESH_TOKEN_INVALID(1008, "无效的刷新Token"),

    // 短信验证码相关 1101-1199
    SMS_SEND_FAILED(1101, "短信发送失败"),
    SMS_CODE_INVALID(1102, "验证码错误"),
    SMS_CODE_EXPIRED(1103, "验证码已过期"),
    SMS_SEND_TOO_FREQUENT(1104, "发送过于频繁，请稍后再试"),
    SMS_CODE_MISMATCH(1105, "验证码不匹配"),
    ;

    private final int code;
    private final String message;
}
