# Apiphany HTTP Contract Invariants

This document defines the semantic guarantees provided by Apiphany across all supported HTTP client implementations.

The goal of these invariants is to ensure:

- deterministic behavior
- implementation-independent semantics
- predictable request/response handling
- consistent behavior across HTTP client providers
- minimal hidden behavior

These guarantees apply regardless of the underlying HTTP client implementation (JDK HttpClient, Apache HttpClient, OkHttp, Spring RestClient, etc.), unless explicitly documented otherwise.

---

## 1. Request Construction Invariants

### 1.1 Explicit configuration always wins

If the user explicitly configures a request property, Apiphany must never silently override it.

This includes:

- headers
- HTTP method
- request body
- query parameters
- timeouts
- redirect behavior
- authentication configuration

#### Example

```java
.header("Accept", "application/json")
```

must always produce:

```http
Accept: application/json
```

regardless of any defaults imposed by the underlying HTTP client.

### 1.2 No hidden semantic headers

Apiphany itself must never inject semantic request headers unless explicitly configured by the user.

#### Allowed automatic headers

These may be added when required for HTTP protocol correctness:

- `Content-Length`
- `Transfer-Encoding`

#### Forbidden automatic headers

Unless explicitly configured:

- `Accept`
- `Accept-Encoding`
- `Content-Type`
- `Authorization`
- `User-Agent`

### 1.3 Request builders are isolated

Request state must never leak between requests.

This includes:

- headers
- query parameters
- authentication state
- cookies
- request bodies

Each request builder must behave independently.

### 1.4 Header insertion order is preserved

If multiple values are added for the same header, Apiphany preserves insertion order.

#### Example

```java
.header("X-Test", "A")
.header("X-Test", "B")
```

must preserve the order `A`, then `B`.

### 1.5 Header names are preserved as supplied

Apiphany must not normalize header names supplied by the user.

This includes:

- case normalization
- separator normalization
- formatting changes

Even if the underlying client normalizes headers internally.

---

## 2. Body Handling Invariants
   
### 2.1 Null body is distinct from empty body

Apiphany treats:

- no body
- empty body (`""`)
- empty byte array

as distinct semantic states.

### 2.2 Known-length bodies use deterministic framing

If the request body length is known in advance, Apiphany:

- sends `Content-Length`
- does not use chunked transfer encoding

### 2.3 Unknown-length bodies use streaming framing

If the request body length is unknown:

- `Transfer-Encoding: chunked` may be used
- unless HTTP/2 semantics make it unnecessary

### 2.4 Request body bytes are never modified silently

Apiphany must never silently alter request body bytes.

This includes:

- implicit compression
- charset rewriting
- BOM insertion/removal
- newline normalization

unless explicitly configured.

---

## 3. Response Handling Invariants
   
### 3.1 Response bodies are preserved byte-for-byte

Except for explicitly handled content encodings, response body bytes are preserved exactly as received.

### 3.2 Content-Encoding decoding is deterministic

If a response specifies:

```http
Content-Encoding: gzip
```

Apiphany decodes the content exactly once.

Recursive or repeated decompression is never performed automatically.

### 3.3 Unknown encodings are ignored safely

Unknown content encodings:

- must not crash response handling
- must not corrupt known encodings

### 3.4 Empty responses are never deserialized

For responses such as:

- `204 No Content`
- `304 Not Modified`
- `Content-Length: 0`

Apiphany must not attempt deserialization.

## 3.5 Charset from `Content-Type` is respected

If the response specifies:

```http
Content-Type: text/plain; charset=ISO-8859-1
```

deserialization must use the declared charset.

---

## 4. Error Handling Invariants
   
### 4.1 Transport failures become `HttpException`

Transport-layer failures are mapped consistently to `HttpException`.

Examples include:

- timeouts
- DNS failures
- TLS failures
- connection resets

### 4.2 HTTP errors are not transport failures

HTTP error responses such as:

- `404`
- `500`

must preserve:

- response body
- response headers
- status code

### 4.3 Error responses remain readable

Response bodies for error responses must remain accessible.

This includes:

- 4xx responses
- 5xx responses

---

## 6. Redirect Invariants
   
### 5.1 Redirect handling is explicit

Apiphany never silently follows redirects unless redirect handling is explicitly enabled.

### 5.2 Redirect semantics follow RFC behavior

Redirect handling follows RFC-defined method semantics.

Examples:

- `307` preserves method and body
- `308` preserves method and body
- `303` converts to GET

---

## 6. Compression Invariants
   
### 6.1 Compression negotiation is opt-in

Apiphany never advertises:

```http
Accept-Encoding
```

unless explicitly configured by the user.

### 6.2 Content-Encoding decoding order follows RFC semantics

For responses such as:

```http
Content-Encoding: gzip, deflate
```

Apiphany decodes encodings in reverse application order.

---

## 7. Concurrency Invariants
   
### 7.1 ApiClient is thread-safe

Concurrent requests must not:

- corrupt shared state
- leak request state
- interfere with each other

### 7.2 ExchangeClient reuse is safe

Connection pooling and client reuse must not leak:

- authentication state
- headers
- cookies
- retry state

between requests.

---

## 8. Authentication Invariants
   
### 8.1 Authentication clients are isolated

Different AuthenticationTypes must remain isolated.

This includes:

- tokens
- sessions
- cookies
- counters
- retry state

### 8.2 Authentication headers are never implicit

Authentication headers must never be added automatically unless an authentication mechanism is explicitly configured.

---

## 9. Serialization Invariants
   
### 9.1 Serialization follows Content-Type

Serialization and deserialization behavior is determined by the declared Content-Type.

Apiphany does not guess serialization formats.

### 9.2 `String` responses do not require `Accept` headers

If a server returns:

```http
Content-Type: text/plain
```

Apiphany can deserialize the response into `String` even when the request did not include:

```http
Accept: text/plain
```

---

## 10. Cross-Client Consistency Invariants
    
### 10.1 Semantic behavior remains consistent across HTTP clients

The following behaviors must remain consistent across supported HTTP client implementations:

- timeout semantics
-  retry behavior
- redirect handling
- decompression behavior
- serialization behavior
- exception mapping
- authentication handling

---

## 11. Non-Invariants

The following behaviors are explicitly outside the scope of Apiphany guarantees:

- TCP packet structure
- TLS fingerprinting
- ALPN negotiation
- HTTP protocol version negotiation
- connection reuse strategies
- JVM-specific transport behavior
- proxy-injected headers
- operating system networking behavior
- default headers imposed by underlying HTTP clients

Examples include:

- JVM-injected User-Agent
- proxy-added forwarding headers
- client-specific connection headers

These behaviors are implementation-specific and intentionally not standardized by Apiphany.
