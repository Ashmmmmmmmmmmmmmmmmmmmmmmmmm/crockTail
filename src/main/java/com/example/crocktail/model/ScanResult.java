package com.example.crocktail.model;

public class ScanResult {
    private String domain;
    private DnsReport dns;
    private IpInfoResult ipInfo;
    private SslCertResult sslCert;
    private WhoisResult whois;

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
}
