# Known Gaps

This document tracks known contract/docs/implementation gaps.

## 1. Planned

### 1.1 Native asynchronous transport IO is not implemented

`ApiClient.asyncExchange(ApiRequest)` provides asynchronous retry, metrics, logging, duration tracking, and exception
handling. `ExchangeClient.asyncExchange(ApiRequest)` runs the synchronous transport exchange on Morphix's shared
virtual-thread executor, so it does not block the calling thread.

The transports still use their synchronous IO APIs internally. Native asynchronous IO, such as JDK
`HttpClient.sendAsync`, Apache HttpClient async, or a Spring reactive transport, remains planned.

- `ExchangeClient.java`
- `ApiClient.java`

### 1.2 OAuth2ApiClient only implements CLIENT_CREDENTIALS grant

Only `CLIENT_CREDENTIALS` is supported. `AUTHORIZATION_CODE`, `REFRESH_TOKEN`, `JWT_BEARER`, and `DEVICE_CODE` throw `UnsupportedOperationException`. `ClientAuthenticationMethod.NONE` (public clients) is also unhandled.

- `OAuth2ApiClient.java:40,174-187`
- `AuthorizationGrantType.java:17-37`

### 1.3 Refresh token flow: data model exists, flow missing

`OAuth2TokenProvider` has a TODO for refresh token functionality. Supporting model already built but unused: `AuthenticationToken.refreshToken`, `OAuth2Parameter.REFRESH_TOKEN`, `AuthorizationGrantType.REFRESH_TOKEN`.

- `OAuth2TokenProvider.java:38`

## 2. Medium

### 2.1 RSAKeys: generation/saving/loading not implemented

Only PEM reading exists (`loadPEMPublicKey`, `loadPEMPPrivateKey`). No keypair generation or PEM writing despite the class being named "RSA key management utility."

- `RSAKeys.java:23`

### 2.2 TLS 1.3 PRF algorithms unsupported

`PRF.algorithmName()` silently defaults anything other than SHA256/SHA384 to HMAC-SHA256, even though `SSLProtocol.TLS_1_3` exists.

- `PRF.java:24,29-34`

### 2.3 Unsupported cipher suites hard-blocked with no provider fallback

`UNSUPPORTED_SUN_JCE_BULK_CIPHERS` throws `SecurityException`. TODO suggests BouncyCastle support. Deprecated cipher constants linger without removal plan.

- `BulkCipher.java:169,179,316-320`
- `CipherSuite.java:303,310`
- `SignatureAlgorithm.java:110,116`

### 2.4 OpenSSL TLS handshake ITs stranded in wrong module

Tests depend on externally started OpenSSL server on port 4433, skipped via `assumeTrue` when unreachable. An `integration/apiphany-openssl-rand-tests` directory exists with only C sources/shell scripts — no Maven Java test module was created.

- `TLSObjectIT.java:123,141`

### 2.5 Metrics cache is unbounded (LRU never implemented)

`METERS_CACHE` is a plain `ConcurrentHashMap` keyed by generated meter names — potential unbounded growth for applications generating dynamic operation names.

- `BasicMeters.java:91-95`

### 2.6 Content-type header parsing limitations

Missing support for version params (`application/*`), format params (`text/*`), multiple header values in a single string, and strict parsing mode that throws on malformed input. Currently `parse(String)` returns null silently.

- `HttpContentType.java:20,268-269`

### 2.7 Tracing hardcoded to B3 only

`getTracingHeaders()` reads only `traceId`/`spanId` from SLF4J MDC and emits `b3-traceid`/`b3-spanid`. W3C `traceparent`, Jaeger, etc. unsupported.

- `HttpExchangeClient.java:148-164`

### 2.8 Content converter registration/discovery not abstracted

Inline `// TODO: abstract away the converter registration and discovery` over the `JSON_CONVERTERS` pair-list loop with manual `noJsonConverterFound` flag.

- `AbstractHttpExchangeClient.java:103-119`

## 3. Low

### 3.1 ExchangeLogger rough edges

Missing multi-line header logging for readability and injectable exchange logger (currently static-only, users cannot plug in custom logging).

- `ExchangeLogger.java:26-27`

### 3.2 HeaderRole categorization idea never done

TODO to add a `HeaderRole` enum to categorize headers (e.g., `CREDENTIAL`, `CONTEXT`, `SECURITY_POLICY`, `TRACING`, `OTHER`).

- `DeFactoHeader.java:18`

### 3.3 Test-infrastructure TODOs

`TestSummary` is self-declared rough implementation. `ForkedJvmRunner` cannot run parameterized tests or lifecycle hooks (`@BeforeEach`/`@AfterEach`).

- `TestSummary.java:26`
- `ForkedJvmRunner.java:79-80`

### 3.4 ClientHello SNI validation unhandled

TODO to handle invalid SNI in OpenSSL-record parsing test.

- `ClientHelloTest.java:91`

### 3.5 Deprecated API surface without replacement metadata

All `@Deprecated` markers lack `since`/`forRemoval`. Worth tracking for eventual cleanup.

- `HttpStatus.java:161`
- `MessageDigestAlgorithm.java:81,87`
- `SSLProtocol.java:75,90`
- `HttpAuthenticationScheme.java:92`
- `BulkCipher.java:169,179`
- `CipherSuite.java:303,310`
- `SignatureAlgorithm.java:110,116`
- `DeFactoHeader.X_XSS_PROTECTION:97,170`

### 3.6 README advertises clients that don't exist yet

README states "Support is being developed for other HTTP clients like Netty, OkHttp, Jersey, etc." No such modules exist in the repo. `docs/apiphany-contract-invariants.md` also lists OkHttp among guaranteed clients with no implementation.

- `README.md:46`
- `docs/apiphany-contract-invariants.md:13`

### 3.7 Disabled debugging test left in tree

Debug test with `@Disabled` annotation is duplicate coverage kept for debugging purposes.

- `TLSObjectLegacyCipherIT.java:112`

### 3.8 Weak failure diagnostics in OpenSSL ITs

Exceptions are caught and logged, then `assertNotNull(serverFinished)` fails with a bare null message instead of propagating the real handshake exception.

- `TLSObjectIT.java:112-117,132-135`
