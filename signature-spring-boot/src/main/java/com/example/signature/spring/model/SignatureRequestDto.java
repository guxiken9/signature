package com.example.signature.spring.model;

import com.example.signature.core.model.SignatureMetadata;
import com.example.signature.core.model.SignatureOptions;
import com.example.signature.core.model.SignatureRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

public class SignatureRequestDto {
    @NotBlank
    private final String mime;

    @NotBlank
    private final String data;

    private final SignatureMetadata metadata;

    @Valid
    private final SignatureOptionsDto options;

    public SignatureRequestDto(String mime, String data, SignatureMetadata metadata, SignatureOptionsDto options) {
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

    public SignatureOptionsDto getOptions() {
        return options;
    }

    public SignatureRequest toCoreModel() {
        SignatureOptions coreOptions = null;
        if (options != null) {
            coreOptions = new SignatureOptions(
                options.getOutputFormat(),
                options.getBackgroundColor(),
                options.getTrimTransparent(),
                options.getWidth(),
                options.getHeight()
            );
            coreOptions.validate();
        }
        return new SignatureRequest(mime, data, metadata, coreOptions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignatureRequestDto that = (SignatureRequestDto) o;
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
        return "SignatureRequestDto{" +
               "mime='" + mime + '\'' +
               ", data='" + (data != null ? data.substring(0, Math.min(50, data.length())) + "..." : "null") + '\'' +
               ", metadata=" + metadata +
               ", options=" + options +
               '}';
    }
}
