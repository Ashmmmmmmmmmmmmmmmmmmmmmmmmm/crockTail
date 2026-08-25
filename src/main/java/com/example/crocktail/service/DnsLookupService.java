package com.example.crocktail.service;

import com.example.crocktail.model.DnsReport;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;

@Service
public class DnsLookupService {

    /**
     * Look up a single DNS record type for the given domain
     * @param domain target domain, e.g. "example.com"
     * @param type record type, e.g. Type.A, Type.MX, Type.NS, Type.TXT
     * @return list of record values as strings, or empty list if none found
     */
    public List<String> lookup(String domain, int type) {
        List<String> results = new ArrayList<>();
        try {
            Lookup lookup = new Lookup(domain, type);
            org.xbill.DNS.Record[] records = lookup.run();

            if (records != null) {
                for (org.xbill.DNS.Record record : records) {
                    results.add(record.rdataToString());
                }
            }
        } catch (TextParseException e) {
            results.add("Invalid domain format: " + e.getMessage());
        }
        return results;
    }

    /**
     * Look up the common record types for a domain in one call
     */
    public DnsReport fullLookup(String domain) {
        DnsReport report = new DnsReport();
        report.setA(lookup(domain, Type.A));
        report.setAaaa(lookup(domain, Type.AAAA));
        report.setMx(lookup(domain, Type.MX));
        report.setNs(lookup(domain, Type.NS));
        report.setTxt(lookup(domain, Type.TXT));
        return report;
    }
}
