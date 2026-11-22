# Multi-value query parameters

A query parameter can have more than one value, for example a list of tags. There is no single standard way to put a list in a URL,
so Apiphany lets you choose with a `MultiValueStrategy`.

The default is `MULTI`: the parameter name is repeated once per value.

```
tags = [a, b, c]

MULTI  ->  tags=a&tags=b&tags=c
CSV    ->  tags=a,b,c
SSV    ->  tags=a b c
TSV    ->  tags=a<TAB>b<TAB>c
PIPES  ->  tags=a|b|c
```

## Using it with a parameters object

Put the values in a POJO and add `@QueryParam` on the field (or the getter) to pick the strategy.

```java
public class SearchParams {

    @QueryParam(strategy = MultiValueStrategy.Value.CSV)
    private List<String> tags = List.of("a", "b", "c");

    private String query = "x";

    // no annotation = MULTI
    private List<Integer> ids = List.of(1, 2, 3);
}
```

```java
client()
        .get()
        .path("search")
        .params(new SearchParams())
        .retrieve(Result.class)
        .orDefault(Result::new);
```

Request: `.../search?tags=a,b,c&query=x&ids=1&ids=2&ids=3`

Rules:

- A field without `@QueryParam` behaves like `MULTI`.
- `null` fields and empty collections are left out.
- Lists, sets, arrays (including `int[]`) and any `Iterable` are supported.

## Choosing the strategy

There are two ways, both on `@QueryParam`:

| How                                            | Result  |
|------------------------------------------------|---------|
| `strategy = "multi"` (or `Value.MULTI`)        | `MULTI` |
| `strategy = "csv"`                             | `CSV`   |
| `strategy = "ssv"`                             | `SSV`   |
| `strategy = "tsv"`                             | `TSV`   |
| `strategy = "pipes"`                           | `PIPES` |
| `style = FORM, mode = EXPLODE` (the default)   | `MULTI` |
| `style = FORM, mode = JOINED`                  | `CSV`   |
| `style = SPACE_DELIMITED`                      | `SSV`   |
| `style = PIPE_DELIMITED`                       | `PIPES` |

If `strategy` is set it wins over `style` and `mode`. An unknown strategy name falls back to `MULTI`.

## Without an object

You can also give the strategy directly:

```java
client()
        .get()
        .path("search")
        .param("tags", List.of("a", "b", "c"))                          // MULTI
        .param("ids", List.of(1, 2, 3), MultiValueStrategy.CSV)         // CSV
        .retrieve(Result.class)
        .orDefault(Result::new);
```

Or build the parameters map yourself:

```java
Map<String, List<String>> params = RequestParameters.of(
        parameter("tags", List.of("a", "b", "c"), MultiValueStrategy.PIPES));
```

## How it works

`MULTI` stores all the values under one name in the parameters map. `RequestParameters.asString` then writes one
`name=value` pair for each value. The other strategies join the values with their separator into one value first.

```java
Map<String, List<String>> params = RequestParameters.from(new SearchParams());
String query = RequestParameters.asString(params);                  // tags=a,b,c&query=x&ids=1&ids=2&ids=3
String encoded = RequestParameters.asString(RequestParameters.encode(params));
```

Encoding is applied after joining, so the separators are encoded too: `CSV` gives `tags=a%2Cb%2Cc`, `TSV` gives `%09` and
`PIPES` gives `%7C`. A space inside a value is encoded as `+`.

## Where it is tested

- [`RequestParametersTest.MultiValueStrategyTests`](../apiphany-core/src/test/java/org/apiphany/RequestParametersTest.java) for the full
  object-to-string behavior of every strategy
- [`MultiValueStrategyTest`](../apiphany-core/src/test/java/org/apiphany/openapi/MultiValueStrategyTest.java) for the strategies on their own
