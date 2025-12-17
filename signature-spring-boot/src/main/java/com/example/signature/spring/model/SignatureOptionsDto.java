package com.example.signature.spring.model;

import javax.validation.constraints.Min;
import java.util.Objects;

public class SignatureOptionsDto {
    private final String outputFormat;
    private final String backgroundColor;
    private final Boolean trimTransparent;

    @Min(1)
    private final Integer width;

    @Min(1)
    private final Integer height;

    public SignatureOptionsDto(String outputFormat, String backgroundColor, Boolean trimTransparent,
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureOptionsDto that = (SignatureOptionsDto) o;
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
        return "SignatureOptionsDto{" +
               "outputFormat='" + outputFormat + '\'' +
               ", backgroundColor='" + backgroundColor + '\'' +
               ", trimTransparent=" + trimTransparent +
               ", width=" + width +
               ", height=" + height +
               '}';
    }
}
