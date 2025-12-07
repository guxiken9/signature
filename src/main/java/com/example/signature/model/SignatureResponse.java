package com.example.signature.model;

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
