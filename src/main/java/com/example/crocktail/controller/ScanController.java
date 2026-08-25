package com.example.crocktail.controller;

import com.example.crocktail.model.*;
import com.example.crocktail.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ScanController {

    private final DnsLookupService dnsLookupService;
    private final IpInfoService ipInfoService;
    private final SslCertService sslCertService;
    private final WhoisService whoisService;

    public ScanController(DnsLookupService dnsLookupService,
                           IpInfoService ipInfoService,
                           SslCertService sslCertService,
                           WhoisService whoisService) {
        this.dnsLookupService = dnsLookupService;
        this.ipInfoService = ipInfoService;
        this.sslCertService = sslCertService;
        this.whoisService = whoisService;
    }

    @GetMapping("/scan")
    public ScanResult scan(@RequestParam String domain) {
        String cleanDomain = UrlNormalizer.extractDomain(domain);

        ScanResult result = new ScanResult();
        result.setDomain(cleanDomain);

        DnsReport dnsReport = dnsLookupService.fullLookup(cleanDomain);
        result.setDns(dnsReport);

        List<String> aRecords = dnsReport.getA();
        if (aRecords != null && !aRecords.isEmpty()) {
            IpInfoResult ipInfo = ipInfoService.lookup(aRecords.get(0));
            result.setIpInfo(ipInfo);
        }

        SslCertResult sslCert = sslCertService.inspect(cleanDomain);
        result.setSslCert(sslCert);

        WhoisResult whois = whoisService.lookup(cleanDomain);
        result.setWhois(whois);

        return result;
    }
}
