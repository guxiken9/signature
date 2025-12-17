package com.example.signature.core.config;

public class SignatureConfig {
    private long maxPayloadBytes = 2_000_000;

    public SignatureConfig() {
    }

    public SignatureConfig(long maxPayloadBytes) {
        this.maxPayloadBytes = maxPayloadBytes;
    }

    public long getMaxPayloadBytes() {
        return maxPayloadBytes;
    }

    public void setMaxPayloadBytes(long maxPayloadBytes) {
        this.maxPayloadBytes = maxPayloadBytes;
    }
}
