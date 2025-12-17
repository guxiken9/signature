package com.example.signature.core.model;

import java.util.Objects;

public class SignatureOptions {
    private final String outputFormat;
    private final String backgroundColor;
    private final Boolean trimTransparent;
    private final Integer width;
    private final Integer height;

    public SignatureOptions(String outputFormat, String backgroundColor, Boolean trimTransparent,
                           Integer width, Integer height) {
        this.outputFormat = outputFormat;
        this.backgroundColor = backgroundColor;
        this.trimTransparent = trimTransparent;
        this.width = width;
        this.height = height;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public Boolean getTrimTransparent() {
        return trimTransparent;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String resolvedOutputFormat() {
        return (outputFormat == null || outputFormat.trim().isEmpty()) ? "png" : outputFormat.toLowerCase();
    }

    public String resolvedBackgroundColor() {
        return (backgroundColor == null || backgroundColor.trim().isEmpty()) ? "#FFFFFF" : backgroundColor;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureOptions that = (SignatureOptions) o;
        return Objects.equals(outputFormat, that.outputFormat) &&
               Objects.equals(backgroundColor, that.backgroundColor) &&
               Objects.equals(trimTransparent, that.trimTransparent) &&
               Objects.equals(width, that.width) &&
               Objects.equals(height, that.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputFormat, backgroundColor, trimTransparent, width, height);
    }

    @Override
    public String toString() {
        return "SignatureOptions{" +
               "outputFormat='" + outputFormat + '\'' +
               ", backgroundColor='" + backgroundColor + '\'' +
               ", trimTransparent=" + trimTransparent +
               ", width=" + width +
               ", height=" + height +
               '}';
    }
}
