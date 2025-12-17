package com.example.signature.spring.config;

import com.example.signature.core.config.SignatureConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "signature")
public class SignatureProperties {
    private long maxPayloadBytes = 2_000_000;

    public long getMaxPayloadBytes() {
        return maxPayloadBytes;
    }

    public void setMaxPayloadBytes(long maxPayloadBytes) {
        this.maxPayloadBytes = maxPayloadBytes;
    }

    public SignatureConfig toConfig() {
        return new SignatureConfig(this.maxPayloadBytes);
    }
}
