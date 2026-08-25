# crockTail

**crockTail** is a passive OSINT (Open Source Intelligence) reconnaissance tool. Given a target domain, it aggregates publicly available information — DNS records, IP/ASN ownership, SSL/TLS certificate details, and domain registration data — into a single consolidated report.

This project is built as a personal learning project during a software development internship, combining an interest in cybersecurity/red-teaming with hands-on full-stack development practice.

---

## Scope & Ethics

crockTail is strictly a **passive** OSINT tool. It only queries publicly available third-party data sources (DNS resolvers, IP/ASN registries, RDAP registries) and performs a standard TLS handshake to read certificate metadata — the same interaction any web browser performs when visiting a site over HTTPS.

**crockTail does NOT perform:**

- Port scanning
- Service/version fingerprinting
- Vulnerability scanning
- Any form of active probing against the target infrastructure beyond a standard TLS handshake

This tool is intended for authorized security research, educational purposes, and reconnaissance on domains you own or have explicit permission to assess. Users are responsible for ensuring their use of this tool complies with applicable laws and regulations.

---

## Features (V1)

Given a target domain, crockTail returns:

| Module | Data Source | What it reveals |
| --- | --- | --- |
| **DNS Lookup** | Public DNS resolvers | A/AAAA, MX, NS, and TXT records |
| **IP Info** | ip-api.com | Geolocation, ISP, and ASN (Autonomous System) ownership of the resolved IP |
| **SSL/TLS Certificate** | Direct TLS handshake (port 443) | Issuer, validity period, and all domains listed in the certificate's SAN (Subject Alternative Name) field — often revealing related subdomains |
| **WHOIS / RDAP** | rdap.org | Domain registrar, registration date, expiration date, and nameservers |

All four modules are aggregated into a single JSON report via one API call.

---

## Tech Stack

- **Backend:** Java 17, Spring Boot 4.1 (Spring Web)
- **DNS resolution:** dnsjava
- **JSON handling:** Jackson (via `spring-boot-starter-jackson`)
- **HTTP client:** Spring `RestTemplate`
- **Frontend:** Vue 3 (planned — not yet implemented in V1)
- **Build tool:** Maven

---

## Project Structure

```
crockTail/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/crocktail/
│   │   │   ├── CrocktailApplication.java   # Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   └── ScanController.java     # Exposes GET /scan
│   │   │   ├── service/
│   │   │   │   ├── DnsLookupService.java
│   │   │   │   ├── IpInfoService.java
│   │   │   │   ├── SslCertService.java
│   │   │   │   └── WhoisService.java
│   │   │   └── model/
│   │   │       ├── DnsReport.java
│   │   │       ├── IpInfoResult.java
│   │   │       ├── SslCertResult.java
│   │   │       ├── WhoisResult.java
│   │   │       └── ScanResult.java         # Aggregated response shape
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── frontend/                                # Planned for V2
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven (bundled via `.mvn` wrapper)

### Run the backend

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080` by default.

### Query the API

```
GET /scan?domain=example.com
```

Returns a JSON report combining DNS, IP, SSL certificate, and WHOIS/RDAP data for the given domain.

**Example:**

```
http://localhost:8080/scan?domain=google.com
```

---

## Known Limitations (V1)

- No frontend yet — results are only viewable as raw JSON (via browser, Postman, or a JSON-formatting browser extension).
- No caching — repeated queries for the same domain re-fetch all data every time.
- No rate limiting on outbound requests to third-party APIs.
- Limited error resilience — a slow/unresponsive third-party source can delay the full report rather than failing gracefully with a partial result.
- IP/ASN lookups may return incomplete data if the resolved IP falls outside the public internet range (e.g. due to DNS interception on certain networks).

---

## Roadmap (V2 ideas)

- [ ]  Vue 3 frontend for a browsable report view
- [ ]  Subdomain enumeration via certificate transparency logs (crt.sh)
- [ ]  Response caching (in-memory or SQLite)
- [ ]  Per-module timeout and graceful partial-failure handling
- [ ]  Unit tests for each service module
- [ ]  `.env`based API key management for future integrations (e.g. Shodan, IPinfo)

---

## Disclaimer

This tool is provided for educational and authorized security research purposes only. The author assumes no liability for misuse. Always ensure you have permission to assess a target before running reconnaissance against it.
