package com.example.crocktail.model;

import java.util.List;

public class SslCertResult {
    private String issuer;
    private String subject;
    private String validFrom;
    private String validTo;
    private List<String> sanDomains;
    private String error;
    private Long daysUntilExpiry;

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public Long getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(Long daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getValidFrom() { return validFrom; }
    public void setValidFrom(String validFrom) { this.validFrom = validFrom; }

    public String getValidTo() { return validTo; }
    public void setValidTo(String validTo) { this.validTo = validTo; }

    public List<String> getSanDomains() { return sanDomains; }
    public void setSanDomains(List<String> sanDomains) { this.sanDomains = sanDomains; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }


    private String source = "Direct TLS handshake (port 443)";

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }


}
