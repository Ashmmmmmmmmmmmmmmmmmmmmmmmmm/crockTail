package com.example.crocktail.service;

import com.example.crocktail.model.SslCertResult;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class SslCertService {

    /**
     * Perform a standard TLS handshake against the target domain's port 443
     * and read the certificate metadata.
     * @param domain target domain, e.g. "example.com" (no https:// prefix)
     */
    public SslCertResult inspect(String domain) {
        SslCertResult result = new SslCertResult();

        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try (SSLSocket socket = (SSLSocket) factory.createSocket(domain, 443)) {
                socket.setSoTimeout(8000);
                socket.startHandshake();

                Certificate[] certs = socket.getSession().getPeerCertificates();
                X509Certificate cert = (X509Certificate) certs[0];

                result.setIssuer(cert.getIssuerX500Principal().getName());
                result.setSubject(cert.getSubjectX500Principal().getName());
                result.setValidFrom(cert.getNotBefore().toString());
                result.setValidTo(cert.getNotAfter().toString());
                result.setSanDomains(extractSanDomains(cert));
            }
        } catch (Exception e) {
            result.setError("Certificate retrieval failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * Extract the domains listed in the certificate's Subject Alternative Name field
     */
    private List<String> extractSanDomains(X509Certificate cert) throws Exception {
        List<String> domains = new ArrayList<>();
        Collection<List<?>> sanEntries = cert.getSubjectAlternativeNames();

        if (sanEntries != null) {
            for (List<?> entry : sanEntries) {
                Integer type = (Integer) entry.get(0);
                if (type == 2) { // type 2 = dNSName
                    domains.add((String) entry.get(1));
                }
            }
        }
        return domains;
    }
}
