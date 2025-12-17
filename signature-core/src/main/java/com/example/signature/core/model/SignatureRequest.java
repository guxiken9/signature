package com.example.signature.core.model;

public record SignatureRequest(
        String mime,
        String data,
        SignatureMetadata metadata,
        SignatureOptions options
) {
    public SignatureOptions resolvedOptions() {
        return options == null ? new SignatureOptions(null, null, null, null, null) : options;
    }
}
