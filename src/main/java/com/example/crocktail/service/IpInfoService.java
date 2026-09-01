package com.example.crocktail.service;

import com.example.crocktail.model.IpInfoResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class IpInfoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RetryHelper retryHelper;
    public IpInfoService(RetryHelper retryHelper) {
        this.retryHelper = retryHelper;
    }

    /**
     * Look up geolocation and ASN ownership info for the given IP
     * @param ip target IP address, e.g. "93.184.216.34"
     */
    public IpInfoResult lookup(String ip) {
        IpInfoResult result = new IpInfoResult();
        result.setIp(ip);

        // Check if this IP falls in a known reserved/test range first
        if (isReservedTestAddress(ip)) {
            result.setSuspectedIntercepted(true);
            result.setCountry("Suspected interception (reserved test range, not a real public IP)");
            return result;
        }

        result.setSuspectedIntercepted(false);

        try {
            String url = "http://ip-api.com/json/" + ip + "?fields=status,country,city,isp,org,as";

            String response = retryHelper.withRetry(
                    () -> restTemplate.getForObject(url, String.class),
                    3,
                    800
            );

            JsonNode node = objectMapper.readTree(response);

            if ("success".equals(node.path("status").asText())) {
                result.setCountry(node.path("country").asText());
                result.setCity(node.path("city").asText());
                result.setIsp(node.path("isp").asText());
                result.setOrg(node.path("org").asText());
                result.setAsNumber(node.path("as").asText());
            }
        } catch (Exception e) {
            result.setCountry("Lookup failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * Check whether an IP falls inside a range IANA reserves for testing/documentation.
     * These ranges should never appear in a real public DNS resolution result;
     * seeing one usually means the DNS query was intercepted and rewritten in transit.
     */
    private boolean isReservedTestAddress(String ip) {
        // 198.18.0.0/15 -- network device benchmarking (RFC 2544)
        if (ip.startsWith("198.18.") || ip.startsWith("198.19.")) {
            return true;
        }
        // 192.0.2.0/24, 198.51.100.0/24, 203.0.113.0/24 -- documentation (RFC 5737)
        if (ip.startsWith("192.0.2.") || ip.startsWith("198.51.100.") || ip.startsWith("203.0.113.")) {
            return true;
        }
        // 10.0.0.0/8, 192.168.0.0/16 -- private address space (RFC 1918)
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        return false;
    }
}
