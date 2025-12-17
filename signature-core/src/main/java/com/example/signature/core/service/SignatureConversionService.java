package com.example.signature.core.service;

import com.example.signature.core.config.SignatureConfig;
import com.example.signature.core.exception.SignatureProcessingException;
import com.example.signature.core.model.ConversionResult;
import com.example.signature.core.model.SignatureOptions;
import com.example.signature.core.model.SignatureRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignatureConversionService {
    private static final Pattern DATA_URL_PATTERN = Pattern.compile("^data:(.+?);base64,(.+)$");
    private static final ThreadLocal<SimpleDateFormat> FILE_ID_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        }
    };
    private final SignatureConfig config;

    public SignatureConversionService(SignatureConfig config) {
        this.config = config;
    }

    public ConversionResult convert(SignatureRequest request) {
        SignatureOptions options = request.resolvedOptions();
        String targetFormat = sanitizeFormat(options.resolvedOutputFormat());
        byte[] decoded = decodePayload(request.getData());
        BufferedImage source = readImage(decoded);

        BufferedImage processed = source;
        if (options.shouldTrimTransparent()) {
            processed = trimTransparentPixels(processed);
        }

        processed = resizeImage(processed, options.getWidth(), options.getHeight());

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
            if (decoded.length > config.getMaxPayloadBytes()) {
                throw new SignatureProcessingException("INVALID_PAYLOAD", "Payload exceeds max size of " + config.getMaxPayloadBytes());
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
        if ("png".equals(normalized)) {
            return "png";
        } else if ("jpg".equals(normalized) || "jpeg".equals(normalized)) {
            return "jpeg";
        } else {
            throw new SignatureProcessingException("UNSUPPORTED_FORMAT", "Format " + normalized + " is not supported");
        }
    }

    private byte[] writeImage(BufferedImage image, String format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            boolean success = ImageIO.write(image, format, baos);
            if (!success) {
                throw new SignatureProcessingException("UNSUPPORTED_FORMAT", "ImageIO writer for " + format + " not available");
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new SignatureProcessingException("INTERNAL_ERROR", "Unable to write image", ex);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private String contentTypeFor(String format) {
        return format.equals("png") ? "image/png" : "image/jpeg";
    }

    private BufferedImage resizeImage(BufferedImage source, Integer targetWidth, Integer targetHeight) {
        if (targetWidth == null && targetHeight == null) {
            return source;
        }

        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();

        Dimension newSize = calculateDimensions(
            originalWidth, originalHeight,
            targetWidth, targetHeight
        );

        int newWidth = newSize.width;
        int newHeight = newSize.height;

        if (newWidth == originalWidth && newHeight == originalHeight) {
            return source;
        }

        BufferedImage resized = new BufferedImage(
            newWidth, newHeight,
            source.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : source.getType()
        );

        Graphics2D g2d = resized.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                               RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                               RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                               RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            g2d.drawImage(source, 0, 0, newWidth, newHeight, null);
        } finally {
            g2d.dispose();
        }

        return resized;
    }

    private Dimension calculateDimensions(int originalWidth, int originalHeight,
                                         Integer targetWidth, Integer targetHeight) {
        double aspectRatio = (double) originalWidth / originalHeight;

        int newWidth;
        int newHeight;

        if (targetWidth != null && targetHeight != null) {
            double widthRatio = (double) targetWidth / originalWidth;
            double heightRatio = (double) targetHeight / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);

            newWidth = (int) Math.round(originalWidth * ratio);
            newHeight = (int) Math.round(originalHeight * ratio);
        } else if (targetWidth != null) {
            newWidth = targetWidth;
            newHeight = (int) Math.round(targetWidth / aspectRatio);
        } else {
            newHeight = targetHeight;
            newWidth = (int) Math.round(targetHeight * aspectRatio);
        }

        return new Dimension(newWidth, newHeight);
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
        String timestamp = FILE_ID_FORMATTER.get().format(new Date());
        return "sig_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
