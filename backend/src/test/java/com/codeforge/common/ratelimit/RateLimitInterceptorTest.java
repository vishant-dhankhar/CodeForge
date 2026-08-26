package com.codeforge.common.ratelimit;

import com.codeforge.common.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private RateLimitInterceptor interceptor;

    static class SampleController {
        @RateLimit(limit = 5, windowSeconds = 60, keyPrefix = "test")
        public void rateLimitedEndpoint() {}

        public void unrestrictedEndpoint() {}
    }

    private HandlerMethod limitedHandlerMethod;
    private HandlerMethod unrestrictedHandlerMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        SampleController controller = new SampleController();
        Method limitedMethod = SampleController.class.getMethod("rateLimitedEndpoint");
        Method unrestrictedMethod = SampleController.class.getMethod("unrestrictedEndpoint");

        limitedHandlerMethod = new HandlerMethod(controller, limitedMethod);
        unrestrictedHandlerMethod = new HandlerMethod(controller, unrestrictedMethod);
    }

    @Test
    @DisplayName("Should allow execution when endpoint is not rate limited")
    void shouldAllowUnrestrictedEndpoint() {
        boolean result = interceptor.preHandle(request, response, unrestrictedHandlerMethod);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should allow execution when rate limiter returns true")
    void shouldAllowWhenUnderLimit() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimiterService.isAllowed(anyString(), eq(5), eq(60))).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, limitedHandlerMethod);
        assertTrue(result);
    }

    @Test
    @DisplayName("Should throw RateLimitExceededException when rate limiter returns false")
    void shouldThrowExceptionWhenRateLimitExceeded() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(rateLimiterService.isAllowed(anyString(), eq(5), eq(60))).thenReturn(false);

        assertThrows(RateLimitExceededException.class, () ->
                interceptor.preHandle(request, response, limitedHandlerMethod));
    }
}
