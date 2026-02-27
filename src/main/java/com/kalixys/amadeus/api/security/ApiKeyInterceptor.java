package com.kalixys.amadeus.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String UNAUTHORIZED_RESPONSE =
        "{\"error\":\"unauthorized\",\"message\":\"invalid or missing api key\"}";

    private final ApiKeyProperties apiKeyProperties;

    public ApiKeyInterceptor(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String providedApiKey = request.getHeader(apiKeyProperties.getHeaderName());
        if (apiKeyProperties.getValue().equals(providedApiKey)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(UNAUTHORIZED_RESPONSE);
        return false;
    }
}
