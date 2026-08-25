package com.example.crocktail.model;

import java.util.List;

public class SslCertResult {
    private String issuer;
    private String subject;
    private String validFrom;
    private String validTo;
    private List<String> sanDomains;
    private String error;

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

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
}
