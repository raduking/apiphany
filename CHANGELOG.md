## Release Notes

---

`1.0.3`

- Added `Arrays` class for Java array utility methods.
- Added `Strings.envelope` method to add a a string as both a prefix and a suffix to a given string.
- Added `Certificates` class for key stores and trust stores utility methods.
- Added `CertificateStoreInfo` which holds all information to construct a key store or a trust store.
- Renamed `HttpProperties` to `JavaNetHttpProperties`.
- Added SSL capabilities to the HTTP exchange clients.
- Added `SSLProperties` to be able to configure SSL for the HTTP exchange clients.
- Added `SSLProtocol` which enumerates SSL protocols.
- Added `Hex` class that helps with conversions between hexadecimal Strings and bytes or byte arrays.
- Added `ByteSizeable` to represent an object that can report its size in bytes.
- Added `BinaryRepresentable` to represent an object that can be converted to a binary (byte array) representation.
- Added `Bytes` with utility methods to work with bytes.
- Added a new package `org.apiphany.io` for I/O classes.
- Added `BytesWrapper` an immutable container for raw byte data with serialization capabilities.
- Added `Int8`, `Int16`, `Int24`, `Int32`, `Int64` for 8, 16, 24, 32 and 64 bit integers.

---

`1.0.2`

- Renamed `ClientProperties.Timeout.DISABLED` to `ClientProperties.Timeout.INFINTE`.
- Added `ClientProperties.Timeout.ZERO` (which actually also means infinite timeout).
- Upgraded parent to `3.5.3`.
- Renamed `BearerTokenProperties` to `TokenProperties`.
- Changed `TokenProperties.token` to `TokenProperties.value`.
- Added `TokenProperties.authenticationScheme` to be able to configure it for a `TokenHttpExchangeClient`.
- Changed `TokenHttpExchangeClient` to use `TokenProperties.authenticationScheme` and defaults to `HttpAuthScheme.BEARER` if missing.
- Renamed `ApiClient.NO_BASE_URL` to `ApiClient.EMPTY_BASE_URL`.
- Added `Pair` record to hold pairs of objects.
- Added managed and unmanaged exchange clients support to `ApiClient`.
- Added `ApiClient.exchangeClient` to create exchange client builder.
- Added `ApiClient.with` as an alias to `ApiClient.exchangeClient` to create exchange client builder.
- Added `ExchangeClientBuilder` for managed exchange clients in `ApiClient`.
- Changed the default exchange client management to not managed when the `ApiClient` is built with direct `ExchangeClient` objects.
- Added `ApiPage.of` to construct `ApiPage` objects.
- Added `OAuth2ExchangeClientBuilder` to build exchange clients with built in OAuth2 functionality.
- Added `Pair.toMap` to convert a `Pair` object to a `Map` object.
- Changed the main constructor in `ApiClient` to have an exchange client map with for exchange client life cycle management information.
- Added `ApiPage.of` factory method with `ApiPage` class and content.
- Fixed `Headers.addTo` method.

---

`1.0.1`

- Extracted `ParameterFunction` inner class from `RequestParameters` class.
- Upgraded parent to `3.5.0` and other dependencies defined in it.
- Added `MeterRegistry` to `ApiClient`.
- Added `MeterRegistry` to most methods in `BasicMeters`.
- Changed `ClientProperties` to use `Duration` instead of `int` (milliseconds).
- Renamed `HttpExchangeClient` to `JavaNetHttpExchangeClient` to emphasize the underlying HTTP client.
- Added `HttpExchangeClient` interface with HTTP specific methods.
- Removed HTTP specific methods from `ExchangeClient` those are now available in `HttpExchangeClient`.
- Added `HttpClientFluentAdapter` class with all HTTP methods.
- Added `ApiClientFluentAdapter.http` method to directly support HTTP methods like `get`, `put`, `post`, etc.
- Removed HTTP specific methods from `ApiClientFluentAdapter` those are now available in `HttpClientFluentAdapter`.
- Renamed `ClientProperties.Connection.DEFAULT_MAX_TOTAL_CONNECTIONS` to `ClientProperties.Connection.DEFAULT_MAX_TOTAL`.
- Renamed `ClientProperties.Connection.DEFAULT_MAX_PER_ROUTE_CONNECTIONS` to `ClientProperties.Connection.DEFAULT_MAX_PER_ROUTE`.
- Renamed `ClientProperties.Timeout.connectTimeout` to `ClientProperties.Timeout.connect`. 
- Renamed `ClientProperties.Timeout.connectionRequestTimeout` to `ClientProperties.Timeout.connectionRequest`. 
- Renamed `ClientProperties.Timeout.socketTimeout` to `ClientProperties.Timeout.socket`.
- Renamed all the builder methods as well to match all the above fields.
- Moved `getRedactedHeaderPredicate` method from `AbstractHttpExchangeClient` to `HttpExchangeClient` interface.
- Removed all HTTP methods from `AbstractHttpExchangeClient` since they are already present in the interface `HttpExchangeClient`.
- Changed `ExchangeClient` to extend `AutoCloseable` so that all exchange clients need to implement a `close` method for graceful shutdowns.
- Changed `ApiClient` to implement `AutoCloseable` so that all API clients can be gracefully shut down.

---

`1.0.0`

- First release.

---


