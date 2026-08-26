package com.codeforge.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ConflictException extends RuntimeException {

    private final String errorCode = "RESOURCE_CONFLICT";
    private final HttpStatus httpStatus = HttpStatus.CONFLICT;

    public ConflictException(String message) {
        super(message);
    }
}
