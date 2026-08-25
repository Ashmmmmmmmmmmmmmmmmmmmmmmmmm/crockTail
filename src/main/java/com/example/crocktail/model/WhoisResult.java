package com.example.crocktail.model;

public class WhoisResult {
    private String domain;
    private String whoisServer;
    private String rawData;
    private String error;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getWhoisServer() { return whoisServer; }
    public void setWhoisServer(String whoisServer) { this.whoisServer = whoisServer; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
