package com.example.signature.core.exception;

public class SignatureProcessingException extends RuntimeException {
    private final String code;

    public SignatureProcessingException(String code, String message) {
        super(message);
        this.code = code;
    }

    public SignatureProcessingException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
