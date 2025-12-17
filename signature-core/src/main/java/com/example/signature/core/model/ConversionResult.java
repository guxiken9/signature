package com.example.signature.core.model;

public record ConversionResult(
        String fileId,
        String contentType,
        byte[] data,
        int width,
        int height
) {
    public long sizeBytes() {
        return data == null ? 0 : data.length;
    }
}
