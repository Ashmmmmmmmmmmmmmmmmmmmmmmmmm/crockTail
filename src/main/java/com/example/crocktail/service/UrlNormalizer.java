package com.example.crocktail.service;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlNormalizer {

    /**
     * Normalize any user input (full URL / with www / bare domain) down to
     * a clean domain name.
     * Examples:
     *   "https://www.google.com/search?q=1" -> "google.com"
     *   "www.example.com" -> "example.com"
     *   "google.com" -> "google.com"
     */
    public static String extractDomain(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String trimmed = input.trim();
        String host;

        try {
            String toParse = trimmed.contains("://") ? trimmed : "https://" + trimmed;
            URI uri = new URI(toParse);
            host = uri.getHost();
        } catch (URISyntaxException e) {
            return trimmed;
        }

        if (host == null) {
            return trimmed;
        }

        if (host.toLowerCase().startsWith("www.")) {
            host = host.substring(4);
        }

        return host;
    }
}
