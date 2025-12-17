package com.example.signature.core.model;

public record SignatureMetadata(
        Integer width,
        Integer height,
        Integer dpi,
        Integer strokeCount,
        Long durationMs
) {
}
