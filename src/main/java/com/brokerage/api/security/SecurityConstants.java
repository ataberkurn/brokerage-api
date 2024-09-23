package com.brokerage.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityConstants {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration-time}")
    private long expirationTime;

    public String getSecret() {
        return secret;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }
}
