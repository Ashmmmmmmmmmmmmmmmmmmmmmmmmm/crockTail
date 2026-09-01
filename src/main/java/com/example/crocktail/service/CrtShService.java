package com.example.crocktail.service;

import com.example.crocktail.model.CrtShResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class CrtShService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RetryHelper retryHelper;

    public CrtShService(RetryHelper retryHelper) {
        this.retryHelper = retryHelper;
    }

    public CrtShResult lookup(String domain) {
        CrtShResult result = new CrtShResult();
        String url = "https://crt.sh/?q=" + domain + "&output=json";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "crockTail-OSINT-Tool");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String response = retryHelper.withRetry(
                    () -> restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class).getBody(),
                    3,
                    800
            );

            JsonNode root = objectMapper.readTree(response);
            Set<String> subdomains = new LinkedHashSet<>();

            for (JsonNode entry : root) {
                String nameValue = entry.path("name_value").asText();
                for (String name : nameValue.split("\n")) {
                    subdomains.add(name.trim().toLowerCase());
                }
            }

            result.setSubdomains(subdomains.stream().toList());
            result.setCertificateCount(root.size());

        } catch (Exception e) {
            result.setError("crt.sh lookup failed: " + e.getMessage());
        }

        return result;
    }
}