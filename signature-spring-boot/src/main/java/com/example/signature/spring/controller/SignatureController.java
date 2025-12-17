package com.example.signature.spring.controller;

import com.example.signature.core.model.ConversionResult;
import com.example.signature.core.model.SignatureRequest;
import com.example.signature.core.service.SignatureConversionService;
import com.example.signature.spring.model.SignatureRequestDto;
import com.example.signature.spring.model.SignatureResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/signatures")
public class SignatureController {

    private final SignatureConversionService conversionService;

    public SignatureController(SignatureConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SignatureResponse convert(@Valid @RequestBody SignatureRequestDto request) {
        SignatureRequest coreRequest = request.toCoreModel();
        ConversionResult result = conversionService.convert(coreRequest);
        return SignatureResponse.from(result);
    }
}
