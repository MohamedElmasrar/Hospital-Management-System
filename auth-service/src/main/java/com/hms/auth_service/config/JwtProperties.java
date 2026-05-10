package com.hms.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth-service.jwt")
public record JwtProperties(
    String secret, long accessTokenExpirySeconds, int refreshTokenExpiryDays) {}
