package com.example.crocktail.model;

import java.util.List;

public class BatchScanResult {
    private List<ScanResult> results;

    public List<ScanResult> getResults() { return results; }
    public void setResults(List<ScanResult> results) { this.results = results; }
}