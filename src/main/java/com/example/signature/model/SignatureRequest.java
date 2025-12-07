package com.example.signature.model;

import jakarta.validation.constraints.NotBlank;

public record SignatureRequest(
        @NotBlank String mime,
        @NotBlank String data,
        SignatureMetadata metadata,
        SignatureOptions options
) {
    public SignatureOptions resolvedOptions() {
        return options == null ? new SignatureOptions(null, null, null) : options;
    }
}
