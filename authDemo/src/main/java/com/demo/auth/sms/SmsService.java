package com.demo.auth.sms;

import cn.hutool.core.util.RandomUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.demo.auth.common.enums.ErrorCode;
import com.demo.auth.common.exception.BusinessException;
import com.demo.auth.config.SmsProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_SEND_LOCK_PREFIX = "sms:lock:";
    private static final long SEND_COOLDOWN_SECONDS = 60; // 60秒内不可重复发送

    private final SmsProperties smsProperties;
    private final StringRedisTemplate redisTemplate;

    private Client client;

    @PostConstruct
    public void init() {
        try {
            Config config = new Config()
                    .setAccessKeyId(smsProperties.getAccessKeyId())
                    .setAccessKeySecret(smsProperties.getAccessKeySecret());
            // 访问域名，国内短信使用 dysmsapi.aliyuncs.com
            config.endpoint = "dysmsapi.aliyuncs.com";
            this.client = new Client(config);
            log.info("阿里云短信客户端初始化成功");
        } catch (Exception e) {
            log.error("阿里云短信客户端初始化失败", e);
            // 不抛出异常，允许应用启动，发送短信时会报错
        }
    }

    @PreDestroy
    public void shutdown() {
        // 阿里云SDK的Client实现了TeaCloseable，这里做清理
        try {
            if (this.client != null) {
                this.client.close();
            }
        } catch (Exception e) {
            log.warn("关闭短信客户端时出错", e);
        }
    }

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     */
    public void sendVerificationCode(String phone) {
        // 1. 检查发送频率（60秒内不能重复发送）
        String lockKey = SMS_SEND_LOCK_PREFIX + phone;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", SEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(ErrorCode.SMS_SEND_TOO_FREQUENT);
        }

        // 2. 生成6位随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 3. 存储验证码到Redis（关联手机号）
        String codeKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(
                codeKey,
                code,
                smsProperties.getExpireMinutes(),
                TimeUnit.MINUTES
        );

        // 4. 调用阿里云发送短信
        try {
            sendSms(phone, code);
            log.info("短信验证码已发送: phone={}, code={}", phone, code);
        } catch (Exception e) {
            log.error("短信发送失败: phone={}", phone, e);
            // 发送失败时清理Redis中的验证码和锁
            redisTemplate.delete(codeKey);
            redisTemplate.delete(lockKey);
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED, e.getMessage());
        }
    }

    /**
     * 校验验证码（校验后立即删除，一次性使用）
     *
     * @param phone 手机号
     * @param code  验证码
     * @return true=验证通过
     */
    public boolean verifyCode(String phone, String code) {
        String codeKey = SMS_CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            throw new BusinessException(ErrorCode.SMS_CODE_EXPIRED);
        }

        // 校验后立即删除（一次性使用）
        redisTemplate.delete(codeKey);

        if (!storedCode.equals(code)) {
            throw new BusinessException(ErrorCode.SMS_CODE_MISMATCH);
        }

        return true;
    }

    /**
     * 调用阿里云短信API
     */
    private void sendSms(String phone, String code) throws Exception {
        if (client == null) {
            // 开发模式：如果没有配置阿里云，直接打印验证码到日志
            log.warn("阿里云短信客户端未初始化，验证码: phone={}, code={}（仅开发环境）", phone, code);
            return;
        }

        SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(smsProperties.getSignName())
                .setTemplateCode(smsProperties.getTemplateCode())
                .setTemplateParam("{\"code\":\"" + code + "\"}");

        SendSmsResponse response = client.sendSms(request);

        String responseCode = response.getBody().getCode();
        if (!"OK".equals(responseCode)) {
            throw new RuntimeException("阿里云返回错误: code=" + responseCode
                    + ", message=" + response.getBody().getMessage());
        }
    }
}
