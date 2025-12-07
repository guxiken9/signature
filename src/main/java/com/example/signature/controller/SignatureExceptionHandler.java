package com.example.signature.controller;

import com.example.signature.exception.SignatureProcessingException;
import com.example.signature.model.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SignatureExceptionHandler {

    @ExceptionHandler(SignatureProcessingException.class)
    public ResponseEntity<ApiError> handleSignature(SignatureProcessingException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "UNSUPPORTED_FORMAT" -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case "INVALID_PAYLOAD", "INVALID_OPTIONS" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return new ResponseEntity<>(ApiError.of(status.value(), ex.getCode(), ex.getMessage()), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage() == null ? err.getCode() : err.getDefaultMessage())
                .orElse("Validation error");
        return new ResponseEntity<>(ApiError.of(status.value(), "INVALID_PAYLOAD", message), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(ApiError.of(status.value(), "INTERNAL_ERROR", ex.getMessage()), status);
    }
}
