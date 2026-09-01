package com.example.crocktail.service;

import com.example.crocktail.model.ConfidenceResult;
import com.example.crocktail.model.Observation;
import com.example.crocktail.model.ScanResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiskAssessmentService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Observation> assess(ScanResult result) {
        List<Observation> observations = new ArrayList<>();

        checkSslExpiry(result, observations);
        checkDomainAge(result, observations);
        checkCrtShVolume(result, observations);
        checkDnsConsistency(result, observations);

        return observations;
    }

    private void checkSslExpiry(ScanResult result, List<Observation> observations) {
        if (result.getSslCert() == null || result.getSslCert().getDaysUntilExpiry() == null) return;

        long daysLeft = result.getSslCert().getDaysUntilExpiry();
        if (daysLeft < 0) {
            observations.add(new Observation("warning", "Certificate has already expired"));
        } else if (daysLeft <= 15) {
            observations.add(new Observation("warning", "Certificate expires in " + daysLeft + " days"));
        }
    }

    private void checkDomainAge(ScanResult result, List<Observation> observations) {
        if (result.getWhois() == null || result.getWhois().getRawData() == null) return;

        try {
            JsonNode root = objectMapper.readTree(result.getWhois().getRawData());
            JsonNode events = root.path("events");

            for (JsonNode event : events) {
                if ("registration".equals(event.path("eventAction").asText())) {
                    String dateStr = event.path("eventDate").asText();
                    Instant registeredAt = Instant.parse(dateStr);
                    long daysOld = ChronoUnit.DAYS.between(registeredAt, Instant.now());

                    if (daysOld < 30) {
                        observations.add(new Observation("warning", "Domain recently registered (" + daysOld + " days ago)"));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            // rawData couldn't be parsed as expected; skip this check silently
        }
    }

    private void checkCrtShVolume(ScanResult result, List<Observation> observations) {
        if (result.getCrtSh() == null) return;

        int certCount = result.getCrtSh().getCertificateCount();
        if (certCount > 50) {
            observations.add(new Observation("info", "High certificate reissuance history (" + certCount + " certificates on record)"));
        }

        if (result.getCrtSh().getSubdomains() != null && result.getCrtSh().getSubdomains().size() > 1) {
            observations.add(new Observation("info", "Historical subdomains detected (" + result.getCrtSh().getSubdomains().size() + " found via crt.sh)"));
        }
    }

    private void checkDnsConsistency(ScanResult result, List<Observation> observations) {
        if (result.getCrossVerification() == null) return;

        if (!result.getCrossVerification().isConsistent()) {
            observations.add(new Observation("warning", "DNS resolvers disagree — " + result.getCrossVerification().getNote()));
        }
    }

    public ConfidenceResult calculateConfidence(ScanResult result) {
        ConfidenceResult confidence = new ConfidenceResult();
        int score = 100;
        List<String> penalties = new ArrayList<>();

        // Each failed module lowers confidence
        if (result.getSslCert() != null && result.getSslCert().getError() != null) {
            score -= 20;
            penalties.add("SSL data unavailable");
        }
        if (result.getWhois() != null && result.getWhois().getError() != null) {
            score -= 20;
            penalties.add("WHOIS data unavailable");
        }
        if (result.getCrtSh() != null && result.getCrtSh().getError() != null) {
            score -= 10;
            penalties.add("crt.sh data unavailable");
        }

        // DNS inconsistency is a strong signal, penalize more
        if (result.getCrossVerification() != null && !result.getCrossVerification().isConsistent()) {
            score -= 25;
            penalties.add("DNS resolvers disagree");
        }

        score = Math.max(score, 0);

        if (score >= 80) {
            confidence.setLevel("high");
        } else if (score >= 50) {
            confidence.setLevel("medium");
        } else {
            confidence.setLevel("low");
        }

        confidence.setScore(score);
        confidence.setExplanation(penalties.isEmpty() ? "All sources responded consistently" : String.join(", ", penalties));

        return confidence;
    }
}