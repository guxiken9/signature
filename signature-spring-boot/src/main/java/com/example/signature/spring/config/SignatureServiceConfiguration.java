package com.example.signature.spring.config;

import com.example.signature.core.service.SignatureConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SignatureServiceConfiguration {

    @Bean
    public SignatureConversionService signatureConversionService(SignatureProperties properties) {
        return new SignatureConversionService(properties.toConfig());
    }
}
