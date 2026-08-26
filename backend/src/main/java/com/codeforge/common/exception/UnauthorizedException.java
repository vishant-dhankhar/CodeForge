package com.codeforge.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final String errorCode = "UNAUTHORIZED";
    private final HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;

    public UnauthorizedException(String message) {
        super(message);
    }
}
