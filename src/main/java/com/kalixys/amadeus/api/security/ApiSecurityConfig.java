package com.kalixys.amadeus.api.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(ApiKeyProperties.class)
public class ApiSecurityConfig implements WebMvcConfigurer {

    private final ApiKeyProperties apiKeyProperties;

    public ApiSecurityConfig(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiKeyInterceptor(apiKeyProperties))
            .addPathPatterns("/api/**");
    }
}
