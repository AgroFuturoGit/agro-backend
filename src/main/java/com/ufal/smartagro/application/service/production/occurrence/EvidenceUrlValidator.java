package com.ufal.smartagro.application.service.production.occurrence;

import java.net.URI;

final class EvidenceUrlValidator {

    private EvidenceUrlValidator() {}

    static String normalizeAndValidate(String rawUrl) {
        if (rawUrl == null) {
            return null;
        }
        String trimmed = rawUrl.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("A URL de evidência deve usar protocolo http:// ou https:// válido.");
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new IllegalArgumentException("A URL de evidência deve usar protocolo http:// ou https:// válido.");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("A URL de evidência deve conter um domínio válido.");
        }
        return trimmed;
    }
}
