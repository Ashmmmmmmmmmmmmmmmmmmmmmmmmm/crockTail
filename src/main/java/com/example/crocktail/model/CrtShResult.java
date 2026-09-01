package com.example.crocktail.model;

import java.util.List;

public class CrtShResult {
    private List<String> subdomains;
    private int certificateCount;
    private String source = "crt.sh (Certificate Transparency logs)";
    private String error;

    public List<String> getSubdomains() { return subdomains; }
    public void setSubdomains(List<String> subdomains) { this.subdomains = subdomains; }

    public int getCertificateCount() { return certificateCount; }
    public void setCertificateCount(int certificateCount) { this.certificateCount = certificateCount; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}