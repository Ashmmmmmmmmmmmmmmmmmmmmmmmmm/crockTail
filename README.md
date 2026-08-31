# crockTail

**crockTail** is a passive OSINT (Open Source Intelligence) reconnaissance tool. Given a target domain, it aggregates publicly available information — DNS records, IP/ASN ownership, SSL/TLS certificate details, and domain registration data — into a single consolidated report.

This project is built as a personal learning project during a software development internship, combining an interest in cybersecurity/red-teaming with hands-on full-stack development practice.

---

## What it does

Enter a domain, hit scan, get a single report combining four modules:

| Module | Source | What you get |
| --- | --- | --- |
| **DNS** | Public resolvers (dnsjava) | A, AAAA, MX, NS, TXT records |
| **IP Info** | ip-api.com | Geolocation, ISP, ASN ownership |
| **SSL/TLS** | Direct TLS handshake (443) | Issuer, validity window, all SAN domains — often reveals related subdomains |
| **WHOIS / RDAP** | rdap.org | Registrar, registration/expiry dates, nameservers |

All four run per query and get aggregated into one JSON response via a single `/scan` call.

## Scope & Ethics

crockTail is **strictly passive**. It only talks to public third-party data sources and performs a standard TLS handshake — the same thing a browser does when it loads a site over HTTPS.

It does **not**:

- Port scan
- Fingerprint services or versions
- Scan for vulnerabilities
- Actively probe target infrastructure beyond a normal TLS handshake

Built for authorized security research, education, and reconnaissance on domains you own or have explicit permission to assess. You're responsible for using it lawfully.

## Tech stack

**Backend** — Java 17, Spring Boot, dnsjava (DNS resolution), Jackson (JSON), RestTemplate (HTTP client), Maven

**Frontend** — Vue 3 + Vite, single-page console-style UI with tabbed report view

## Project structure

```
crocktail/
├── src/main/java/com/example/crocktail/
│   ├── CrocktailApplication.java
│   ├── controller/
│   │   └── ScanController.java       # GET /scan
│   ├── service/
│   │   ├── DnsLookupService.java
│   │   ├── IpInfoService.java
│   │   ├── SslCertService.java
│   │   ├── WhoisService.java
│   │   └── UrlNormalizer.java
│   └── model/
│       ├── DnsReport.java
│       ├── IpInfoResult.java
│       ├── SslCertResult.java
│       ├── WhoisResult.java
│       └── ScanResult.java           # aggregated response shape
└── frontend/                          # Vue 3 UI
```

## Getting started

**Requirements:** Java 17+, Node.js, Maven (bundled via `.mvn` wrapper)

**Run the backend**

```bash
./mvnw spring-boot:run
```

Starts on `http://localhost:8080`.

**Run the frontend**

```bash
cd frontend
npm install
npm run dev
```

**Or query the API directly**

```
GET /scan?domain=example.com
```

## Roadmap

- [x] Batch queries
- [x] Per-result source attribution
- [x] Query logging
- [ ] Cross-verification across multiple data sources (crt.sh, Shodan/Censys passive data) for higher-confidence results
- [ ] Response caching
- [ ] Per-module timeout + graceful partial-failure handling
- [ ] Unit tests per service module

## Disclaimer

For educational and authorized security research purposes only. The author assumes no liability for misuse. Always confirm you have permission to assess a target before running reconnaissance against it.


