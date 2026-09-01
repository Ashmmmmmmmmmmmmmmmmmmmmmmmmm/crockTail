package com.example.crocktail.service;

import com.example.crocktail.model.CrossVerificationResult;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CrossVerificationService {

    private final DnsLookupService dnsLookupService;

    public CrossVerificationService(DnsLookupService dnsLookupService) {
        this.dnsLookupService = dnsLookupService;
    }

    /**
     * Cross-verify the domain's A record against two independent public resolvers.
     * If both resolvers return the same set of IPs, the DNS result is considered consistent.
     */
    public CrossVerificationResult verifyDns(String domain) {
        CrossVerificationResult result = new CrossVerificationResult();

        List<String> googleA = dnsLookupService.lookupAViaGoogle(domain);
        List<String> cloudflareA = dnsLookupService.lookupAViaCloudflare(domain);

        result.setGoogleDnsA(googleA);
        result.setCloudflareDnsA(cloudflareA);

        Set<String> googleSet = new HashSet<>(googleA);
        Set<String> cloudflareSet = new HashSet<>(cloudflareA);

        if (googleSet.isEmpty() || cloudflareSet.isEmpty()) {
            result.setConsistent(false);
            result.setNote("One or both resolvers returned no result — cannot verify");
        } else if (googleSet.equals(cloudflareSet)) {
            result.setConsistent(true);
            result.setNote("Google and Cloudflare resolvers agree");
        } else {
            result.setConsistent(false);
            result.setNote("Google and Cloudflare resolvers disagree — possible DNS inconsistency");
        }

        return result;
    }
}