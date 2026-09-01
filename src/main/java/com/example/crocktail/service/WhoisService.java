package com.example.crocktail.service;

import com.example.crocktail.model.WhoisResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhoisService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RetryHelper retryHelper;

    public WhoisService(RetryHelper retryHelper) {
        this.retryHelper = retryHelper;
    }

    /**
     * Look up domain registration data via RDAP (the modern, HTTPS-based
     * successor to the legacy WHOIS protocol). rdap.org auto-routes the
     * query to the correct authoritative registry.
     */
    public WhoisResult lookup(String domain) {
        WhoisResult result = new WhoisResult();
        result.setDomain(domain);

        try {
            String url = "https://rdap.org/domain/" + domain;
            String response = retryHelper.withRetry(
                    () -> restTemplate.getForObject(url, String.class),
                    3,
                    800
            );

            result.setWhoisServer("rdap.org (auto-routed)");
            result.setRawData(response);

        } catch (Exception e) {
            result.setError("WHOIS lookup failed: " + e.getMessage());
        }

        return result;
    }
}