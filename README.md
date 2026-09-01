# crockTail

**crockTail** is a passive OSINT (Open Source Intelligence) reconnaissance tool. Given a target domain, it aggregates publicly available information — DNS records, IP/ASN ownership, SSL/TLS certificate details, and domain registration data — into a single consolidated report.

This project is built as a personal learning project during a software development internship, combining an interest in cybersecurity/red-teaming with hands-on full-stack development practice.

---

## What it does

Enter a domain, hit scan, get a single report combining five modules:

| Module | Source | What you get |
|---|---|---|
| **DNS** | Public resolvers (dnsjava) | A, AAAA, MX, NS, TXT records |
| **IP Info** | ip-api.com | Geolocation, ISP, ASN ownership |
| **SSL/TLS** | Direct TLS handshake (443) | Issuer, validity window, days until expiry, all SAN domains — often reveals related subdomains |
| **WHOIS / RDAP** | rdap.org | Registrar, registration/expiry dates, nameservers |
| **Certificate Transparency** | crt.sh | Historical certificates for the domain, and every subdomain ever listed on one — passive subdomain discovery |

All five run per query and get aggregated into one JSON response via a single `/scan` call. Every result also carries a `source` field so you know exactly which upstream data source it came from.

## Risk observations

On top of raw data, crockTail runs a lightweight assessment pass and surfaces plain-language observations, each tagged as a **warning** or **info**:

- ⚠ Certificate expires soon (≤15 days) or has already expired
- ⚠ Domain was registered recently (<30 days ago)
- ℹ Unusually high certificate reissuance history
- ℹ Historical subdomains discovered via certificate transparency logs

This turns the tool from a raw data dump into something that tells you what's actually worth a second look, instead of making you eyeball four different reports yourself.

## Reliability

Every external call (IP lookup, SSL handshake, WHOIS, crt.sh) is wrapped in an automatic retry (3 attempts, 800ms backoff) to absorb transient network failures — common with third-party APIs and raw TLS connections — instead of failing a whole scan on the first hiccup.

## Cross-verification & confidence scoring

crockTail doesn't just report data — it checks itself:

- **Multi-resolver DNS cross-verification** — every domain's A record is independently queried against Google (8.8.8.8) and Cloudflare (1.1.1.1). A mismatch is surfaced as a warning observation ("DNS resolvers disagree"), a signal that can indicate DNS tampering or interception.
- **Confidence score** — each scan gets a 0–100 confidence score (high/medium/low) based on which modules succeeded, whether any retries were needed, and whether the DNS cross-check agreed. Hover the score in the UI to see exactly what it's based on.

This is the difference between a data aggregator and an analysis tool: the goal isn't just to show you results, it's to tell you how much to trust them.

## Other features

- **Batch queries** — scan multiple domains in one request (`POST /scan/batch`), results returned as a list
- **Query logging** — save any scan result to `query_log.txt` on demand (one JSON entry per line). Works for single scans and for individual rows within a batch result

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
│   │   └── ScanController.java       # /scan, /scan/batch, /scan/log
│   ├── service/
│   │   ├── DnsLookupService.java
│   │   ├── IpInfoService.java
│   │   ├── SslCertService.java
│   │   ├── WhoisService.java
│   │   ├── CrtShService.java
│   │   ├── CrossVerificationService.java
│   │   ├── RiskAssessmentService.java  # observations + confidence scoring
│   │   ├── BatchScanService.java
│   │   ├── QueryLogService.java
│   │   ├── RetryHelper.java           # shared retry/backoff logic
│   │   └── UrlNormalizer.java
│   └── model/
│       ├── DnsReport.java
│       ├── IpInfoResult.java
│       ├── SslCertResult.java
│       ├── WhoisResult.java
│       ├── CrtShResult.java
│       ├── CrossVerificationResult.java
│       ├── ConfidenceResult.java
│       ├── Observation.java
│       ├── BatchScanRequest.java
│       ├── BatchScanResult.java
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

| Endpoint | Method | Body | Description |
|---|---|---|---|
| `/scan?domain=example.com` | GET | — | Scan a single domain |
| `/scan/batch` | POST | `{ "domains": ["a.com", "b.com"] }` | Scan multiple domains, returns a list of results |
| `/scan/log` | POST | A `ScanResult` object | Append that result as one JSON line to `query_log.txt` |

## Roadmap

- [x] Batch queries
- [x] Per-result source attribution
- [x] Query logging
- [x] Certificate transparency lookup (crt.sh) for passive subdomain discovery
- [x] Retry/backoff for transient network failures
- [x] Risk observation flags (cert expiry, domain age, reissuance volume)
- [x] Multi-resolver DNS cross-verification (Google vs Cloudflare)
- [x] Confidence scoring per scan


Historical change tracking and report export were considered and deliberately deferred — they need a meaningful backlog of log data to be worth building, which the project doesn't have yet.

## Disclaimer

For educational and authorized security research purposes only. The author assumes no liability for misuse. Always confirm you have permission to assess a target before running reconnaissance against it.



