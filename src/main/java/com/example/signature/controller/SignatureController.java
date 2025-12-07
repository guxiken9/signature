package com.example.signature.controller;

import com.example.signature.model.SignatureRequest;
import com.example.signature.model.SignatureResponse;
import com.example.signature.service.SignatureConversionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signatures")
public class SignatureController {

    private final SignatureConversionService conversionService;

    public SignatureController(SignatureConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SignatureResponse convert(@Valid @RequestBody SignatureRequest request) {
        return SignatureResponse.from(conversionService.convert(request));
    }
}
