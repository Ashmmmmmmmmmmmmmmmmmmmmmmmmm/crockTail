package com.example.crocktail.model;

import java.util.List;

public class DnsReport {
    private List<String> a;
    private List<String> aaaa;
    private List<String> mx;
    private List<String> ns;
    private List<String> txt;

    public List<String> getA() { return a; }
    public void setA(List<String> a) { this.a = a; }

    public List<String> getAaaa() { return aaaa; }
    public void setAaaa(List<String> aaaa) { this.aaaa = aaaa; }

    public List<String> getMx() { return mx; }
    public void setMx(List<String> mx) { this.mx = mx; }

    public List<String> getNs() { return ns; }
    public void setNs(List<String> ns) { this.ns = ns; }

    public List<String> getTxt() { return txt; }
    public void setTxt(List<String> txt) { this.txt = txt; }
}
