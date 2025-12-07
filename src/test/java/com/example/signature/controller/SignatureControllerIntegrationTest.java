package com.example.signature.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SignatureControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void convertValidPngToJpeg() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
              "metadata": {
                "width": 400,
                "height": 200
              },
              "options": {
                "outputFormat": "jpeg",
                "backgroundColor": "#FFFFFF",
                "trimTransparent": true
              }
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileId").exists())
            .andExpect(jsonPath("$.contentType").value("image/jpeg"))
            .andExpect(jsonPath("$.sizeBytes").isNumber())
            .andExpect(jsonPath("$.width").isNumber())
            .andExpect(jsonPath("$.height").isNumber());
    }

    @Test
    void convertValidPngToPng() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
              "options": {
                "outputFormat": "png",
                "trimTransparent": false
              }
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contentType").value("image/png"));
    }

    @Test
    void convertWithMinimalData() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.contentType").value("image/png"));
    }

    @Test
    void returnBadRequestWhenMimeIsBlank() throws Exception {
        String requestBody = """
            {
              "mime": "",
              "data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PAYLOAD"));
    }

    @Test
    void returnBadRequestWhenDataIsBlank() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": ""
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PAYLOAD"));
    }

    @Test
    void returnBadRequestWhenBase64IsInvalid() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": "invalid-base64!!!"
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PAYLOAD"))
            .andExpect(jsonPath("$.message").value("Base64 decode failed"));
    }

    @Test
    void returnUnsupportedMediaTypeWhenFormatIsInvalid() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
              "options": {
                "outputFormat": "webp"
              }
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(jsonPath("$.code").value("UNSUPPORTED_FORMAT"));
    }

    @Test
    void returnBadRequestWhenBackgroundColorIsInvalid() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
              "options": {
                "backgroundColor": "#GGGGGG"
              }
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_OPTIONS"));
    }

    @Test
    void returnBadRequestWhenImageDataIsInvalid() throws Exception {
        String requestBody = """
            {
              "mime": "image/png",
              "data": "bm90X2FuX2ltYWdl"
            }
            """;

        mockMvc.perform(post("/api/signatures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PAYLOAD"));
    }
}
