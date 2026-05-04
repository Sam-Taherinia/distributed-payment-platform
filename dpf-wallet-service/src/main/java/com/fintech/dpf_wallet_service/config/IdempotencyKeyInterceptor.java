package com.fintech.dpf_wallet_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

public class IdempotencyKeyInterceptor implements HandlerInterceptor {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String IDEMPOTENCY_KEY_ATTRIBUTE = "idempotencyKey";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Only enforce on state-changing methods
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        String key = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        if (key == null || key.isBlank()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Missing required header: Idempotency-Key\"}");
            return false;
        }

        if (key.length() > 255) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Idempotency-Key must not exceed 255 characters\"}");
            return false;
        }

        // Store as request attribute so controllers read from one place
        request.setAttribute(IDEMPOTENCY_KEY_ATTRIBUTE, key);
        return true;
    }
}
