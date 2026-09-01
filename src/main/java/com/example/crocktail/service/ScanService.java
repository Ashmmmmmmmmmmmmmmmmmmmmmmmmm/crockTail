package com.example.crocktail.service;

import com.example.crocktail.model.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ScanService {

    private final DnsLookupService dnsLookupService;
    private final IpInfoService ipInfoService;
    private final SslCertService sslCertService;
    private final WhoisService whoisService;
    private final QueryLogService queryLogService;
    private final CrtShService crtShService;
    private final RiskAssessmentService riskAssessmentService;
    private final CrossVerificationService crossVerificationService;

    public ScanService(DnsLookupService dnsLookupService,
                       IpInfoService ipInfoService,
                       SslCertService sslCertService,
                       WhoisService whoisService,
                       QueryLogService queryLogService,
                       CrtShService crtShService,
                       RiskAssessmentService riskAssessmentService,
                       CrossVerificationService crossVerificationService) {
        this.dnsLookupService = dnsLookupService;
        this.ipInfoService = ipInfoService;
        this.sslCertService = sslCertService;
        this.whoisService = whoisService;
        this.queryLogService = queryLogService;
        this.crtShService = crtShService;
        this.riskAssessmentService = riskAssessmentService;
        this.crossVerificationService = crossVerificationService;
    }

    public ScanResult scan(String rawDomain) {
        String cleanDomain = UrlNormalizer.extractDomain(rawDomain);

        ScanResult result = new ScanResult();
        result.setDomain(cleanDomain);

        DnsReport dnsReport = dnsLookupService.fullLookup(cleanDomain);
        result.setDns(dnsReport);

        List<String> aRecords = dnsReport.getA();
        if (aRecords != null && !aRecords.isEmpty()) {
            IpInfoResult ipInfo = ipInfoService.lookup(aRecords.get(0));
            result.setIpInfo(ipInfo);
        }

        result.setSslCert(sslCertService.inspect(cleanDomain));
        result.setWhois(whoisService.lookup(cleanDomain));
        result.setCrtSh(crtShService.lookup(cleanDomain));
        result.setObservations(riskAssessmentService.assess(result));
        result.setCrossVerification(crossVerificationService.verifyDns(cleanDomain));
        result.setConfidence(riskAssessmentService.calculateConfidence(result));

        return result;
    }
}