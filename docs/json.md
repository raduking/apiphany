# JsonBuilder

`JsonBuilder` is the project-wide JSON entry point. Call the static methods; they delegate to whichever JSON library is on the classpath.

```java
String json = JsonBuilder.toJson(value);
Value read = JsonBuilder.fromJson(json, Value.class);
```

`fromJson` accepts a `String`, a `byte[]`, or an `InputStream`. `toJson(null)` returns `null`. Generic types use `fromJson(json, new GenericClass<List<Value>>() {})`.

`toMap` and `fromMap` convert between an object and a `Map<String, Object>`. Both take an error consumer and return an empty result when conversion fails.

`JsonBuilder.with(builder, supplier)` runs the supplier against a different builder on the current thread, then restores the previous one.

## Library selection

The runtime builder is chosen once, in this order:

1. Jackson 3 (`tools.jackson.databind.ObjectMapper`), through `Jackson3JsonBuilder`
2. Jackson 2 (`com.fasterxml.jackson.databind.ObjectMapper`), through `Jackson2JsonBuilder`
3. The base `JsonBuilder`, which writes `toString()` inside an identity JSON object and cannot read JSON back

Jackson is optional. A module that serializes with `JsonBuilder` declares the Jackson artifacts it needs as `provided` and `optional`.

## Configuration

| Property | Effect |
|---|---|
| `json-builder.to-json.indent-output=true` | Pretty-print JSON. `JsonBuilder.indentJsonOutput(boolean)` changes the same flag at runtime. |
| `json-builder.to-json.debug-string=true` | Allows JSON previews in error logs. Do not enable this in production. |

Both Jackson builders apply the same rules:

- Empty beans serialize. Serialization does not fail when a bean has no properties.
- Unknown JSON fields are ignored on read.
- A JSON `null` for a primitive field (`int`, `long`, `boolean`, `double`, and the other primitives) fails the read. `fromJson` logs the error and returns `null`. A missing field keeps the primitive default (`0`, `false`, `0.0`). A null wrapper, such as `Integer`, stays null and the rest of the object is read.
- Enum values and map keys are written and read with `toString()`. The match ignores case. `@AsValue` still selects the annotated member. When `toString()` differs from the constant name, the constant name is not accepted.
- Null properties are omitted, including null map values. Null elements inside a list are kept.
- Properties follow the order of the fields in the class. Jackson 3 disables alphabetical sorting, which is that library's default.
- Date, time, and duration values are ISO-8601 strings, not numeric timestamps. `java.util.Date` and `java.util.Calendar` use a numeric zero offset (`+00:00`). Jackson 3 also pins nanosecond timestamp precision, date map keys as strings, the context time zone on write, and omission of the zone id, so that output stays stable if library defaults change.

`configure(Consumer)` changes the underlying mapper at runtime. Jackson 2 updates the mutable `ObjectMapper` in place. Jackson 3 builds a new `JsonMapper` and replaces the reference.

## Jackson

Jackson 2 and Jackson 3 do not serialize the same JDK types by default. `JsonBuilder` keeps one representation for both, and the custom handlers below exist only where that default differs. `RequestMethod` is separate: both modules register it because it is an Apiphany type, not a JDK parity fix.

Jackson 2 registers `JavaTimeModule` (ISO `LocalDate` and `LocalDateTime`) and `ApiphanyJackson2Module`. It also registers `Jdk8Module` (`Optional`, `OptionalInt`, `OptionalLong`, `OptionalDouble`) when `Jackson2Library.isJdk8ModulePresent()` is true. That module is loaded by name, so `JsonBuilder` still starts when the library is absent; `Optional` values then use Jackson's default handling. `Jdk8Module` does not extend `SimpleModule`, so `registerModule` accepts any Jackson `Module`. Jackson 2 needs `jackson-datatype-jsr310` on the classpath. Jackson 3 registers `ApiphanyJackson3Module`; its `java.time` support is part of databind, and its date/time features are set explicitly so the ISO output does not follow library defaults.

| Type | Jackson 2 default | Jackson 3 default | What is registered |
|---|---|---|---|
| `Locale` value | `toString()` → `"en_US"` | `toLanguageTag()` → `"en-US"` | Value serializer on Jackson 2 only |
| `Locale` map key | `toString()` → `"en_US"` | `toString()` → `"en_US"` | Key serializer on both, so keys match the `"en-US"` value |
| `Year` | string `"2026"` when dates are not timestamps | number `2026`, because its serializer ignores that feature | Serializer on Jackson 3 only |
| `Month` value | enum name `"FEBRUARY"` | number `2` | Serializer on Jackson 3 only |
| `Month` read | names only; one-based numbers are off by default | numbers and exact names (`"FEBRUARY"`), not `"february"` | Deserializer on both |
| `java.sql.Date` value | `toString()` → `"2024-01-02"` | full date-time string, like `java.util.Date` | Serializer on Jackson 3 only |
| `java.sql.Date` map key | treated as `java.util.Date` | treated as `java.util.Date` | Key serializer on both, so keys stay date-only |

The Jackson 3 `Month` deserializer is the one handler that is not required for the JSON `JsonBuilder` writes. Jackson 3 already reads `"FEBRUARY"`, `2`, and `"2"`. The custom deserializer exists so that `"february"` is accepted too. The Jackson 2 `Month` deserializer has to stay: without it, a numeric month does not deserialize.
