package com.example.crocktail.model;

public class IpInfoResult {
    private String ip;
    private String country;
    private String city;
    private String isp;
    private String org;
    private String asNumber;
    private Boolean suspectedIntercepted;

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getIsp() { return isp; }
    public void setIsp(String isp) { this.isp = isp; }

    public String getOrg() { return org; }
    public void setOrg(String org) { this.org = org; }

    public String getAsNumber() { return asNumber; }
    public void setAsNumber(String asNumber) { this.asNumber = asNumber; }

    public Boolean getSuspectedIntercepted() { return suspectedIntercepted; }
    public void setSuspectedIntercepted(Boolean suspectedIntercepted) { this.suspectedIntercepted = suspectedIntercepted; }
}
