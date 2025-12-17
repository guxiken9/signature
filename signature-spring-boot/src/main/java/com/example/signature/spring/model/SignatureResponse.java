package com.example.signature.spring.model;

import com.example.signature.core.model.ConversionResult;

public record SignatureResponse(
        String fileId,
        String contentType,
        long sizeBytes,
        int width,
        int height
) {
    public static SignatureResponse from(ConversionResult result) {
        return new SignatureResponse(result.fileId(), result.contentType(), result.sizeBytes(), result.width(), result.height());
    }
}
