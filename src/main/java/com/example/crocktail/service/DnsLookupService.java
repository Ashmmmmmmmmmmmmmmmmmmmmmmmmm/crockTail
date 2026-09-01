package com.example.crocktail.service;

import com.example.crocktail.model.DnsReport;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DnsLookupService {

    /**
     * Look up a single DNS record type for the given domain, using a specific resolver.
     * @param domain target domain, e.g. "example.com"
     * @param type record type, e.g. Type.A, Type.MX, Type.NS, Type.TXT
     * @param resolverIp the DNS server to ask, e.g. "8.8.8.8" (Google) or "1.1.1.1" (Cloudflare).
     *                    Pass null to use the system default resolver.
     * @return list of record values as strings, or empty list if none found
     */
    public List<String> lookup(String domain, int type, String resolverIp) {
        List<String> results = new ArrayList<>();
        try {
            Lookup lookup = new Lookup(domain, type);

            if (resolverIp != null) {
                Resolver resolver = new SimpleResolver(resolverIp);
                lookup.setResolver(resolver);
            }

            org.xbill.DNS.Record[] records = lookup.run();

            if (records != null) {
                for (org.xbill.DNS.Record record : records) {
                    results.add(record.rdataToString());
                }
            }
        } catch (TextParseException e) {
            results.add("Invalid domain format: " + e.getMessage());
        } catch (UnknownHostException e) {
            results.add("Resolver unreachable: " + e.getMessage());
        }
        return results;
    }

    /**
     * Look up the common record types for a domain using the system default resolver
     */
    public DnsReport fullLookup(String domain) {
        DnsReport report = new DnsReport();
        report.setA(lookup(domain, Type.A, null));
        report.setAaaa(lookup(domain, Type.AAAA, null));
        report.setMx(lookup(domain, Type.MX, null));
        report.setNs(lookup(domain, Type.NS, null));
        report.setTxt(lookup(domain, Type.TXT, null));
        return report;
    }

    /**
     * Look up the A record for a domain via two independent public resolvers,
     * used for cross-verification of DNS consistency.
     */
    public List<String> lookupAViaGoogle(String domain) {
        return lookup(domain, Type.A, "8.8.8.8");
    }

    public List<String> lookupAViaCloudflare(String domain) {
        return lookup(domain, Type.A, "1.1.1.1");
    }
}