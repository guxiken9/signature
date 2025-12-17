package com.example.signature.spring.controller;

import com.example.signature.core.exception.SignatureProcessingException;
import com.example.signature.spring.model.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SignatureExceptionHandler {

    @ExceptionHandler(SignatureProcessingException.class)
    public ResponseEntity<ApiError> handleSignature(SignatureProcessingException ex) {
        HttpStatus status;
        String code = ex.getCode();
        if ("UNSUPPORTED_FORMAT".equals(code)) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        } else if ("INVALID_PAYLOAD".equals(code) || "INVALID_OPTIONS".equals(code)) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<ApiError>(ApiError.of(status.value(), ex.getCode(), ex.getMessage()), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Validation error";
        if (ex.getBindingResult().hasErrors()) {
            Object error = ex.getBindingResult().getAllErrors().get(0);
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                message = fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : fieldError.getCode();
            }
        }
        return new ResponseEntity<ApiError>(ApiError.of(status.value(), "INVALID_PAYLOAD", message), status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<ApiError>(ApiError.of(status.value(), "INVALID_OPTIONS", ex.getMessage()), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<ApiError>(ApiError.of(status.value(), "INTERNAL_ERROR", ex.getMessage()), status);
    }
}
