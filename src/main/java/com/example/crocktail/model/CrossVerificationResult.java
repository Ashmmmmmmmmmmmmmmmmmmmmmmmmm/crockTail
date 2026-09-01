package com.example.crocktail.model;

import java.util.List;

public class CrossVerificationResult {
    private List<String> googleDnsA;
    private List<String> cloudflareDnsA;
    private boolean consistent;
    private String note;

    public List<String> getGoogleDnsA() { return googleDnsA; }
    public void setGoogleDnsA(List<String> googleDnsA) { this.googleDnsA = googleDnsA; }

    public List<String> getCloudflareDnsA() { return cloudflareDnsA; }
    public void setCloudflareDnsA(List<String> cloudflareDnsA) { this.cloudflareDnsA = cloudflareDnsA; }

    public boolean isConsistent() { return consistent; }
    public void setConsistent(boolean consistent) { this.consistent = consistent; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}