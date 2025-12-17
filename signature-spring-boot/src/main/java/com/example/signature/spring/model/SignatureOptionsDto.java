package com.example.signature.spring.model;

import jakarta.validation.constraints.Min;

public record SignatureOptionsDto(
        String outputFormat,
        String backgroundColor,
        Boolean trimTransparent,
        @Min(1) Integer width,
        @Min(1) Integer height
) {
}
