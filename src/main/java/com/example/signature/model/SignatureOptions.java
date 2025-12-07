package com.example.signature.model;

public record SignatureOptions(
        String outputFormat,
        String backgroundColor,
        Boolean trimTransparent
) {
    public String resolvedOutputFormat() {
        return (outputFormat == null || outputFormat.isBlank()) ? "png" : outputFormat.toLowerCase();
    }

    public String resolvedBackgroundColor() {
        return (backgroundColor == null || backgroundColor.isBlank()) ? "#FFFFFF" : backgroundColor;
    }

    public boolean shouldTrimTransparent() {
        return Boolean.TRUE.equals(trimTransparent);
    }
}
