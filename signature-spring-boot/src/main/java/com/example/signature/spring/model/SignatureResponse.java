package com.example.signature.spring.model;

import com.example.signature.core.model.ConversionResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class SignatureResponse {
    private final String fileId;
    private final String contentType;
    private final long sizeBytes;
    private final int width;
    private final int height;

    @JsonCreator
    public SignatureResponse(
            @JsonProperty("fileId") String fileId,
            @JsonProperty("contentType") String contentType,
            @JsonProperty("sizeBytes") long sizeBytes,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height) {
        this.fileId = fileId;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.width = width;
        this.height = height;
    }

    public static SignatureResponse from(ConversionResult result) {
        return new SignatureResponse(
            result.getFileId(),
            result.getContentType(),
            result.sizeBytes(),
            result.getWidth(),
            result.getHeight()
        );
    }

    public String getFileId() {
        return fileId;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureResponse that = (SignatureResponse) o;
        return sizeBytes == that.sizeBytes &&
               width == that.width &&
               height == that.height &&
               Objects.equals(fileId, that.fileId) &&
               Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, contentType, sizeBytes, width, height);
    }

    @Override
    public String toString() {
        return "SignatureResponse{" +
               "fileId='" + fileId + '\'' +
               ", contentType='" + contentType + '\'' +
               ", sizeBytes=" + sizeBytes +
               ", width=" + width +
               ", height=" + height +
               '}';
    }
}
