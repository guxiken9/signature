package com.example.signature.spring.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Objects;

public class ApiError {
    private final Date timestamp;
    private final int status;
    private final String code;
    private final String message;

    @JsonCreator
    public ApiError(
            @JsonProperty("timestamp") Date timestamp,
            @JsonProperty("status") int status,
            @JsonProperty("code") String code,
            @JsonProperty("message") String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static ApiError of(int status, String code, String message) {
        return new ApiError(new Date(), status, code, message);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiError apiError = (ApiError) o;
        return status == apiError.status &&
               Objects.equals(timestamp, apiError.timestamp) &&
               Objects.equals(code, apiError.code) &&
               Objects.equals(message, apiError.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, status, code, message);
    }

    @Override
    public String toString() {
        return "ApiError{" +
               "timestamp=" + timestamp +
               ", status=" + status +
               ", code='" + code + '\'' +
               ", message='" + message + '\'' +
               '}';
    }
}
