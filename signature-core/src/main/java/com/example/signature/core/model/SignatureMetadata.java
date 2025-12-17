package com.example.signature.core.model;

import java.util.Objects;

public class SignatureMetadata {
    private final Integer width;
    private final Integer height;
    private final Integer dpi;
    private final Integer strokeCount;
    private final Long durationMs;

    public SignatureMetadata(Integer width, Integer height, Integer dpi, Integer strokeCount, Long durationMs) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
        this.strokeCount = strokeCount;
        this.durationMs = durationMs;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Integer getDpi() {
        return dpi;
    }

    public Integer getStrokeCount() {
        return strokeCount;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureMetadata that = (SignatureMetadata) o;
        return Objects.equals(width, that.width) &&
               Objects.equals(height, that.height) &&
               Objects.equals(dpi, that.dpi) &&
               Objects.equals(strokeCount, that.strokeCount) &&
               Objects.equals(durationMs, that.durationMs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, dpi, strokeCount, durationMs);
    }

    @Override
    public String toString() {
        return "SignatureMetadata{" +
               "width=" + width +
               ", height=" + height +
               ", dpi=" + dpi +
               ", strokeCount=" + strokeCount +
               ", durationMs=" + durationMs +
               '}';
    }
}
