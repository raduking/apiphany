## Release Notes

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

---

`1.0.0`

- First release.

---


