package com.example.signature.core.service;

import com.example.signature.core.config.SignatureConfig;
import com.example.signature.core.model.SignatureOptions;
import com.example.signature.core.model.SignatureRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureConversionServiceTest {

    private SignatureConversionService service;

    @BeforeEach
    void setup() {
        this.service = new SignatureConversionService(new SignatureConfig());
    }

    @Test
    void convertPngPayload() throws IOException {
        byte[] payload = createSamplePng();
        String base64 = Base64.getEncoder().encodeToString(payload);
        SignatureRequest request = new SignatureRequest(
                "image/png",
                "data:image/png;base64," + base64,
                null,
                new SignatureOptions("png", "#FFFFFF", true, null, null)
        );

        var result = service.convert(request);

        assertThat(result.contentType()).isEqualTo("image/png");
        assertThat(result.sizeBytes()).isGreaterThan(0);
    }

    private byte[] createSamplePng() throws IOException {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, 0, 15, 15);
        g2d.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}
