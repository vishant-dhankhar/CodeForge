package com.codeforge.common.ratelimit;

import com.codeforge.common.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        }

        if (rateLimit == null) {
            return true;
        }

        String clientIdentifier = getClientIdentifier(request);
        String rateLimitKey = String.format("%s:%s:%s", rateLimit.keyPrefix(), handlerMethod.getMethod().getName(), clientIdentifier);

        boolean allowed = rateLimiterService.isAllowed(
                rateLimitKey,
                rateLimit.limit(),
                rateLimit.windowSeconds()
        );

        if (!allowed) {
            throw new RateLimitExceededException(
                    String.format("Too many requests. Limit is %d requests per %d seconds. Please try again later.",
                            rateLimit.limit(), rateLimit.windowSeconds())
            );
        }

        return true;
    }

    private String getClientIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getName();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
