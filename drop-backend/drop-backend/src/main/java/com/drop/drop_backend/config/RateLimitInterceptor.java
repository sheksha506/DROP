package com.drop.drop_backend.config;

import com.drop.drop_backend.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor
        implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(
            RateLimitService rateLimitService
    ) {
        this.rateLimitService =
                rateLimitService;
    }

    // =========================================================
    // BEFORE REQUEST
    // =========================================================

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        String path =
                request.getRequestURI();

        String clientIp =
                getClientIp(
                        request
                );

        int maxRequests;

        String endpoint;

        // =====================================================
        // NEARBY
        // =====================================================

        if (
                path.equals(
                        "/api/places/nearby"
                )
        ) {

            endpoint =
                    "nearby";

            maxRequests =
                    30;

            // =====================================================
            // RANDOM
            // =====================================================

        } else if (
                path.equals(
                        "/api/places/random"
                )
        ) {

            endpoint =
                    "random";

            maxRequests =
                    30;

            // =====================================================
            // RESTAURANT DETAIL
            // =====================================================

        } else if (
                path.matches(
                        "/api/places/[0-9]+"
                )
        ) {

            endpoint =
                    "detail";

            maxRequests =
                    60;

        } else {

            return true;
        }

        // =====================================================
        // CHECK LIMIT
        // =====================================================

        boolean allowed =
                rateLimitService.isAllowed(
                        clientIp,
                        endpoint,
                        maxRequests
                );

        if (!allowed) {

            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS
                            .value()
            );

            response.setHeader(
                    "Retry-After",
                    "60"
            );

            response.setContentType(
                    "application/json"
            );

            try {

                response.getWriter()
                        .write(
                                "{\"error\":\"Too many requests. Please try again later.\"}"
                        );

            } catch (Exception ignored) {
            }

            return false;
        }

        return true;
    }

    // =========================================================
    // CLIENT IP
    // =========================================================

    private String getClientIp(
            HttpServletRequest request
    ) {

        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (
                forwardedFor != null
                        && !forwardedFor.isBlank()
        ) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        String realIp =
                request.getHeader(
                        "X-Real-IP"
                );

        if (
                realIp != null
                        && !realIp.isBlank()
        ) {

            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}