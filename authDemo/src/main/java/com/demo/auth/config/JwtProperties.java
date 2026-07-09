package com.demo.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Base64编码的密钥（至少256位）
     */
    private String secret;

    /**
     * AccessToken过期时间（毫秒），默认24小时
     */
    private long accessTokenExpiration = 86400000L;

    /**
     * RefreshToken过期时间（毫秒），默认7天
     */
    private long refreshTokenExpiration = 604800000L;
}
