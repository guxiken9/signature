package com.example.signature.core.model;

public record SignatureOptions(
        String outputFormat,
        String backgroundColor,
        Boolean trimTransparent,
        Integer width,
        Integer height
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

    public void validate() {
        if (width != null && width <= 0) {
            throw new IllegalArgumentException("Width must be positive");
        }
        if (height != null && height <= 0) {
            throw new IllegalArgumentException("Height must be positive");
        }
    }
}
