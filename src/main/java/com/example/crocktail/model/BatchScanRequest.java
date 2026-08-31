package com.example.crocktail.model;

import java.util.List;

public class BatchScanRequest {
    private List<String> domains;

    public List<String> getDomains() { return domains; }
    public void setDomains(List<String> domains) { this.domains = domains; }
}