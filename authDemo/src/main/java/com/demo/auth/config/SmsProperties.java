package com.demo.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsProperties {

    /**
     * 阿里云AccessKey ID
     */
    private String accessKeyId;

    /**
     * 阿里云AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 验证码模板CODE
     */
    private String templateCode;

    /**
     * 验证码过期时间（分钟）
     */
    private int expireMinutes = 5;
}
