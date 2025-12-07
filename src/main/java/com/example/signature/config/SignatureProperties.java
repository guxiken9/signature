package com.example.signature.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "signature")
public class SignatureProperties {
    /** 最大ペイロードサイズ（バイト） */
    private long maxPayloadBytes = 2_000_000;

    public long getMaxPayloadBytes() {
        return maxPayloadBytes;
    }

    public void setMaxPayloadBytes(long maxPayloadBytes) {
        this.maxPayloadBytes = maxPayloadBytes;
    }
}
