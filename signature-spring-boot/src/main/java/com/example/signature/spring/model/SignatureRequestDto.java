package com.example.signature.spring.model;

import com.example.signature.core.model.SignatureMetadata;
import com.example.signature.core.model.SignatureOptions;
import com.example.signature.core.model.SignatureRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record SignatureRequestDto(
        @NotBlank String mime,
        @NotBlank String data,
        SignatureMetadata metadata,
        @Valid SignatureOptionsDto options
) {
    public SignatureRequest toCoreModel() {
        SignatureOptions coreOptions = null;
        if (options != null) {
            coreOptions = new SignatureOptions(
                options.outputFormat(),
                options.backgroundColor(),
                options.trimTransparent(),
                options.width(),
                options.height()
            );
            coreOptions.validate();
        }
        return new SignatureRequest(mime, data, metadata, coreOptions);
    }
}
