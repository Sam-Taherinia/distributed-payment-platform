package com.fintech.dpf_wallet_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IdempotencyKeyInterceptor())
                // Apply to ALL mutating wallet endpoints
                .addPathPatterns(
                        "/api/v1/wallets",
                        "/api/v1/wallets/*/deposit",
                        "/api/v1/wallets/*/withdraw",
                        "/api/v1/wallets/transfer"
                );
    }
}
