package com.codeforge.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final String errorCode = "RATE_LIMIT_EXCEEDED";
    private final HttpStatus httpStatus = HttpStatus.TOO_MANY_REQUESTS;

    public RateLimitExceededException(String message) {
        super(message);
    }
}
