# Logging Configuration

Apiphany provides fine-grained control over what gets logged during HTTP exchanges. All logging configuration lives under `logging` in `ClientProperties`.

## Configuration Structure

```yaml
logging:
  headers:
    mode: full
    sensitive:
      - X-Custom-Token
  params:
    mode: full
    sensitive:
      - session_id
  body:
    mode: full
    redact: true
```

Each category (`headers`, `params`, `body`) has:

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `mode` | `Mode` | `full` | What content to include in logs |
| `redact` | `Boolean` | `true` | Whether to apply sensitivity redaction (body only) |
| `sensitive` | `List<String>` | `null` | Extra names to redact, case-insensitive (headers/params only) |

## Logging Modes

Defined in `Logging.Mode`:

| Mode | Behavior |
|------|----------|
| `full` | Logs the full value via `toString()` |
| `metadata` | Logs type, length, and truncated SHA-256 hash |
| `none` | Logs `<omitted>` |

`ExchangeLogger` uses `LENGTH` and `HASH` includes for `metadata` mode. The `Logging` utility also supports `Include.PREVIEW` which returns the first 512 characters of the body — do not use it in production logging paths.

## Sensitive Header Redaction

Headers are redacted when their name matches the sensitivity predicate. The predicate is composed from:

1. **`DefaultHttpSensitivity`** hardcoded set:
   - `Authorization`, `Proxy-Authorization`
   - `Cookie`, `Set-Cookie`, `Set-Cookie2`
   - `WWW-Authenticate`
   - `Api-Key`, `X-API-Key`, `X-Auth-Token`, `X-Authorization`

2. **`logging.headers.sensitive`** — additional names, case-insensitive

Matching headers are replaced with `-REDACTED-` in logs.

```yaml
logging:
  headers:
    sensitive:
      - X-Custom-Token
      - X-Internal-Id
```

```java
ClientProperties props = new ClientProperties();
props.getLogging().getHeaders().setSensitive(List.of("X-Custom-Token"));
```

## Sensitive Parameter Redaction

Parameters are redacted when their name matches the sensitivity predicate. The predicate is composed from:

1. **`DefaultHttpSensitivity`** hardcoded set:
   - `token`, `access_token`, `refresh_token`
   - `api_key`, `apikey`
   - `code`, `client_secret`, `password`

2. **`logging.params.sensitive`** — additional names, case-insensitive

Matching parameters are replaced with `-REDACTED-` in logs.

```yaml
logging:
  params:
    sensitive:
      - session_id
```

```java
ClientProperties props = new ClientProperties();
props.getLogging().getParams().setSensitive(List.of("session_id"));
```

## Body Redaction

Body redaction works differently from headers/params because bodies don't have named fields that can be matched generically. The `redact` flag controls whether the sensitivity check runs at all.

### How it works

The redact check runs **before** mode-specific logic. When `redact=true` (default):

1. `ExchangeLogger` calls `ExchangeClient.isSensitiveBody(body)`
2. If the client returns `true`, the body is replaced with `-REDACTED-` regardless of mode
3. If the client returns `false`, the body proceeds to mode-specific rendering

### Default behavior

`DefaultHttpSensitivity.isSensitiveBody()` returns `false` — no auto-detection. There is no generic way to detect sensitive body content without understanding the body's schema.

### Custom sensitivity

Override `isSensitiveBody()` on your exchange client to implement domain-specific logic:

```java
public class MyExchangeClient extends AbstractHttpExchangeClient {

    @Override
    public boolean isSensitiveBody(Object body) {
        if (body instanceof AuthenticationToken) {
            return true;
        }
        if (body instanceof String text) {
            return text.contains("password");
        }
        return false;
    }
}
```

### Disabling redaction

Set `redact=false` on the body category to skip the sensitivity check entirely:

```yaml
logging:
  body:
    redact: false
```

```java
ClientProperties props = new ClientProperties();
props.getLogging().getBody().setRedact(false);
```

When `redact=false`, the body is always rendered via the mode-specific logic regardless of what `isSensitiveBody()` returns.

### Interaction with mode

| Mode | `redact=true` + sensitive | `redact=true` + not sensitive | `redact=false` |
|------|---------------------------|-------------------------------|----------------|
| `full` | `-REDACTED-` | `body.toString()` | `body.toString()` |
| `metadata` | `-REDACTED-` | type + length + hash | type + length + hash |
| `none` | `-REDACTED-` | `<omitted>` | `<omitted>` |

The redact guard runs before mode logic, so it catches sensitive bodies in all modes.

## Configuration Examples

### Safe defaults (production)

```yaml
logging:
  body:
    mode: metadata
```

### Full debugging (development)

```yaml
logging:
  body:
    mode: full
    redact: false
```

```java
ClientProperties props = new ClientProperties();
props.getLogging().getBody().setMode(Logging.Mode.FULL);
props.getLogging().getBody().setRedact(false);
```

### Custom sensitive headers + params

```yaml
logging:
  headers:
    sensitive:
      - X-Internal-Token
  params:
    sensitive:
      - session_id
```

Body redaction is handled by `isSensitiveBody()` override on the exchange client.

## Sensitivity Interfaces

The sensitivity system is built on three functional interfaces:

| Interface | Method | Purpose |
|-----------|--------|---------|
| `HeaderSensitivity` | `isSensitiveHeader(String)` | Tests header names |
| `ParameterSensitivity` | `isSensitiveParameter(String)` | Tests parameter names |
| `BodySensitivity` | `isSensitiveBody(Object)` | Tests body objects |

These are combined in `HttpSensitivity`, implemented by `DefaultHttpSensitivity` (singleton), and wired through `HttpExchangeClient`.
