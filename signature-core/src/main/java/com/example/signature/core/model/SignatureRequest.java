package com.example.signature.core.model;

import java.util.Objects;

public class SignatureRequest {
    private final String mime;
    private final String data;
    private final SignatureMetadata metadata;
    private final SignatureOptions options;

    public SignatureRequest(String mime, String data, SignatureMetadata metadata, SignatureOptions options) {
        this.mime = mime;
        this.data = data;
        this.metadata = metadata;
        this.options = options;
    }

    public String getMime() {
        return mime;
    }

    public String getData() {
        return data;
    }

    public SignatureMetadata getMetadata() {
        return metadata;
    }

    public SignatureOptions getOptions() {
        return options;
    }

    public SignatureOptions resolvedOptions() {
        return options == null ? new SignatureOptions(null, null, null, null, null) : options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureRequest that = (SignatureRequest) o;
        return Objects.equals(mime, that.mime) &&
               Objects.equals(data, that.data) &&
               Objects.equals(metadata, that.metadata) &&
               Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mime, data, metadata, options);
    }

    @Override
    public String toString() {
        return "SignatureRequest{" +
               "mime='" + mime + '\'' +
               ", data='" + (data != null ? data.substring(0, Math.min(50, data.length())) + "..." : "null") + '\'' +
               ", metadata=" + metadata +
               ", options=" + options +
               '}';
    }
}
