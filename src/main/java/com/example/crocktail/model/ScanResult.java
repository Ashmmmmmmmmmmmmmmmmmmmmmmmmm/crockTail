package com.example.crocktail.model;

import java.util.List;

public class ScanResult {
    private String domain;
    private DnsReport dns;
    private IpInfoResult ipInfo;
    private SslCertResult sslCert;
    private WhoisResult whois;
    private CrtShResult crtSh;
    private List<Observation> observations;
    private CrossVerificationResult crossVerification;
    private ConfidenceResult confidence;

    public ConfidenceResult getConfidence() { return confidence; }
    public void setConfidence(ConfidenceResult confidence) { this.confidence = confidence; }

    public List<Observation> getObservations() { return observations; }
    public void setObservations(List<Observation> observations) { this.observations = observations; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public DnsReport getDns() { return dns; }
    public void setDns(DnsReport dns) { this.dns = dns; }

    public IpInfoResult getIpInfo() { return ipInfo; }
    public void setIpInfo(IpInfoResult ipInfo) { this.ipInfo = ipInfo; }

    public SslCertResult getSslCert() { return sslCert; }
    public void setSslCert(SslCertResult sslCert) { this.sslCert = sslCert; }

    public WhoisResult getWhois() { return whois; }
    public void setWhois(WhoisResult whois) { this.whois = whois; }

    public CrtShResult getCrtSh() { return crtSh; }
    public void setCrtSh(CrtShResult crtSh) { this.crtSh = crtSh; }

    public CrossVerificationResult getCrossVerification() { return crossVerification; }
    public void setCrossVerification(CrossVerificationResult crossVerification) { this.crossVerification = crossVerification; }
}
