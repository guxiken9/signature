package com.example.signature.service;

import com.example.signature.config.SignatureProperties;
import com.example.signature.exception.SignatureProcessingException;
import com.example.signature.model.ConversionResult;
import com.example.signature.model.SignatureOptions;
import com.example.signature.model.SignatureRequest;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SignatureConversionService {
    private static final Pattern DATA_URL_PATTERN = Pattern.compile("^data:(.+?);base64,(.+)$");
    private static final DateTimeFormatter FILE_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US);
    private final SignatureProperties properties;

    public SignatureConversionService(SignatureProperties properties) {
        this.properties = properties;
    }

    public ConversionResult convert(SignatureRequest request) {
        SignatureOptions options = request.resolvedOptions();
        String targetFormat = sanitizeFormat(options.resolvedOutputFormat());
        byte[] decoded = decodePayload(request.data());
        BufferedImage source = readImage(decoded);

        BufferedImage processed = source;
        if (options.shouldTrimTransparent()) {
            processed = trimTransparentPixels(processed);
        }

        processed = applyBackground(processed, options.resolvedBackgroundColor(), targetFormat);

        byte[] output = writeImage(processed, targetFormat);
        String contentType = contentTypeFor(targetFormat);
        String fileId = generateFileId();
        return new ConversionResult(fileId, contentType, output, processed.getWidth(), processed.getHeight());
    }

    private byte[] decodePayload(String payload) {
        Matcher matcher = DATA_URL_PATTERN.matcher(payload);
        String base64 = matcher.matches() ? matcher.group(2) : payload;
        try {
            byte[] decoded = Base64.getDecoder().decode(base64);
            if (decoded.length > properties.getMaxPayloadBytes()) {
                throw new SignatureProcessingException("INVALID_PAYLOAD", "Payload exceeds max size of " + properties.getMaxPayloadBytes());
            }
            return decoded;
        } catch (IllegalArgumentException ex) {
            throw new SignatureProcessingException("INVALID_PAYLOAD", "Base64 decode failed", ex);
        }
    }

    private BufferedImage readImage(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new SignatureProcessingException("INVALID_PAYLOAD", "Unsupported image format");
            }
            return image;
        } catch (IOException ex) {
            throw new SignatureProcessingException("INVALID_PAYLOAD", "Unable to read image", ex);
        }
    }

    private String sanitizeFormat(String format) {
        String normalized = (format == null) ? "png" : format.toLowerCase(Locale.US);
        return switch (normalized) {
            case "png" -> "png";
            case "jpg", "jpeg" -> "jpeg";
            default -> throw new SignatureProcessingException("UNSUPPORTED_FORMAT", "Format " + normalized + " is not supported");
        };
    }

    private byte[] writeImage(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            boolean success = ImageIO.write(image, format, baos);
            if (!success) {
                throw new SignatureProcessingException("UNSUPPORTED_FORMAT", "ImageIO writer for " + format + " not available");
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new SignatureProcessingException("INTERNAL_ERROR", "Unable to write image", ex);
        }
    }

    private String contentTypeFor(String format) {
        return format.equals("png") ? "image/png" : "image/jpeg";
    }

    private BufferedImage applyBackground(BufferedImage source, String hexColor, String targetFormat) {
        boolean needsOpaque = targetFormat.equals("jpeg");
        if (!needsOpaque && !hasTransparency(source)) {
            return source;
        }

        Color bg = parseColor(hexColor);
        BufferedImage canvas = new BufferedImage(source.getWidth(), source.getHeight(), needsOpaque ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(bg);
            g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g2d.drawImage(source, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        return canvas;
    }

    private boolean hasTransparency(BufferedImage image) {
        return image.getColorModel().hasAlpha();
    }

    private Color parseColor(String hexColor) {
        try {
            return Color.decode(hexColor);
        } catch (NumberFormatException ex) {
            throw new SignatureProcessingException("INVALID_OPTIONS", "Invalid color value: " + hexColor, ex);
        }
    }

    private BufferedImage trimTransparentPixels(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int top = height, left = width, right = 0, bottom = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = source.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha != 0) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }

        if (right < left || bottom < top) {
            return source;
        }

        int trimmedWidth = right - left + 1;
        int trimmedHeight = bottom - top + 1;
        return source.getSubimage(left, top, trimmedWidth, trimmedHeight);
    }

    private String generateFileId() {
        String timestamp = LocalDateTime.now().format(FILE_ID_FORMATTER);
        return "sig_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
