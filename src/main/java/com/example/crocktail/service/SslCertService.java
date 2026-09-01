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

    private final RetryHelper retryHelper;

    public SslCertService(RetryHelper retryHelper) {
        this.retryHelper = retryHelper;
    }

    /**
     * Perform a standard TLS handshake against the target domain's port 443
     * and read the certificate metadata.
     * @param domain target domain, e.g. "example.com" (no https:// prefix)
     */
    public SslCertResult inspect(String domain) {
        SslCertResult result = new SslCertResult();

        try {
            X509Certificate cert = retryHelper.withRetry(
                    () -> fetchCertificate(domain),
                    3,
                    800
            );

            result.setIssuer(cert.getIssuerX500Principal().getName());
            result.setSubject(cert.getSubjectX500Principal().getName());
            result.setValidFrom(cert.getNotBefore().toString());
            result.setValidTo(cert.getNotAfter().toString());
            result.setSanDomains(extractSanDomains(cert));

            long daysLeft = (cert.getNotAfter().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
            result.setDaysUntilExpiry(daysLeft);

        } catch (Exception e) {
            result.setError("Certificate retrieval failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * Connect to the domain's port 443 and return its X.509 certificate.
     * Wrapped by retryHelper in inspect(), so this can throw freely.
     */
    private X509Certificate fetchCertificate(String domain) {
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try (SSLSocket socket = (SSLSocket) factory.createSocket(domain, 443)) {
                socket.setSoTimeout(8000);
                socket.startHandshake();

                Certificate[] certs = socket.getSession().getPeerCertificates();
                return (X509Certificate) certs[0];
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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