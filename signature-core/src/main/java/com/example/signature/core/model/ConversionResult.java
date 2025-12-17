package com.example.signature.core.model;

import java.util.Arrays;
import java.util.Objects;

public class ConversionResult {
    private final String fileId;
    private final String contentType;
    private final byte[] data;
    private final int width;
    private final int height;

    public ConversionResult(String fileId, String contentType, byte[] data, int width, int height) {
        this.fileId = fileId;
        this.contentType = contentType;
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public String getFileId() {
        return fileId;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long sizeBytes() {
        return data == null ? 0 : data.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversionResult that = (ConversionResult) o;
        return width == that.width &&
               height == that.height &&
               Objects.equals(fileId, that.fileId) &&
               Objects.equals(contentType, that.contentType) &&
               Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fileId, contentType, width, height);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "ConversionResult{" +
               "fileId='" + fileId + '\'' +
               ", contentType='" + contentType + '\'' +
               ", dataLength=" + (data != null ? data.length : 0) +
               ", width=" + width +
               ", height=" + height +
               '}';
    }
}
