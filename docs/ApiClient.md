# Building a REST client

Benefits of building a rest client with this API:

- Authentication: OAuth2, SSL, Bearer Token
- Fluent API (similar to Spring RestClient)
- Metrics support: per client, per request
- Retry support
- Easily extensible (feel free to contribute)

---

## Working with [`ApiClient`](../apiphany-core/src/main/java/org/apiphany/ApiClient.java)

### Existing Service Web API

Let's say we have a service called <b>Awesome</b> that has the following simple REST API:

- URL: `http://awesome.somewhere.com/api/v1/info`
- HTTP method: `GET`
- Content Type: `application/json`
- Response:

```json
{
  "name": "Awesome Service",
  "version": 1.27
}
```

### Create the DTO

This DTO will be used for the returned object.

```java
public class Info {

    private String name;
    private Double version;

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setVersion(final Double version) {
        this.version = version;
    }

    public Double getVersion() {
        return version;
    }
}
```

### Create the REST Client

To create the rest client you need to:

- extend the [`ApiClient`](../apiphany-core/src/main/java/org/apiphany/ApiClient.java) base class
- decide what kind of authentication you need

For this example we will use OAuth2 authentication, by using the
[`OAuth2HttpExchangeClient`](../apiphany-core/src/main/java/org/apiphany/security/oauth2/client/OAuth2HttpExchangeClient.java) as 
one of the parameters in the constructor.
The [`ApiClient`](../apiphany-core/src/main/java/org/apiphany/ApiClient.java) has a lot of constructors one of them requires 2 parameters:

- the base URL
- the exchange client builder used

To configure the exchange client with OAuth2 you will need 
[`OAuth2Properties`](../apiphany-core/src/main/java/org/apiphany/security/oauth2/OAuth2Properties.java) object with all the necessary parameters for `OAuth2` set.
Then you will need a [`ClientProperties`](../apiphany-core/src/main/java/org/apiphany/client/ClientProperties.java) object on which you will set the
[`OAuth2Properties`](../apiphany-core/src/main/java/org/apiphany/security/oauth2/OAuth2Properties.java)
object as a custom properties.

```java
public class AwesomeClient extends ApiClient {

    public AwesomeClient(final ClientProperties properties) {
        super("http://awesome.somewhere.com",
		        properties(properties)
                .secureWith()
                .oauth2());
    }
}
```

That's it, now you have a fully functional REST client with OAuth2 authentication ready to make requests.

### Get the `Info` from `/api/v1/info`

To build a method for calling the path we can use the fluent style API as following:

```java
    public Info getInfo() {
        return client()
                .get()
                .path("api", "v1", "info")
                .retrieve(Info.class)
                .orNull();
    }
```
This will return an `Info` object with all members set from `http://awesome.somewhere.com/api/v1/info` as specified in 
the JSON from the API or `null` if any error occurred.

### Deep dive

- `client()`
  - returns the request builder object
  - the returned object is actually [`ApiClientFluentAdapter`](../apiphany-core/src/main/java/org/apiphany/ApiClientFluentAdapter.java)
    with all the methods necessary to make any request
  - if any method is missing feel free to contribute

### [`ApiClientFluentAdapter`](../apiphany-core/src/main/java/org/apiphany/ApiClientFluentAdapter.java) 

This is one of the 2 classes that enables the fluent API. This class handles everything that has to do for creating the
HTTP request, including returned types, headers, etc

- HTTP methods
  - `get()`, `put()`, `post()`, etc. for all current HTTP methods
  - `method(HttpMethod)` in case you want to make it more explicit ore future new methods

- Path methods
  - `path(String...)` is the most common one which specifies the path with a string array
  - `pathEncoded(String...)` same as `uriPath(String...)` but encodes the final URL
  - `uri(URI)` if you want to build your own URL from scratch
  - `url(String)` if you want a completely different URL

- Header methods
  - `headers(Map<?, ?>)` set the HTTP headers
  - `header(String, String)` set one HTTP header

- Request parameters
  - `params(Map<String, String>)` set the request parameters
  - `params(ParameterFunction...)` set the request parameters using a parameter function - see [`RequestParameters`](../apiphany-core/src/main/java/org/apiphany/RequestParameters.java) class

- Retrieve methods (they return an [`ApiResponse`](../apiphany-core/src/main/java/org/apiphany/ApiResponse.java) object)
  - `retrieve(Class<T>)` specifies the type to be retrieved
  - `retrieve(GenericClass<T>)` specifies a parameterized type to be retrieved

- Many others (details found in separate sections)

### [`ApiResponse`](../apiphany-core/src/main/java/org/apiphany/ApiResponse.java) 

This is the other class that handles how the response is actually interpreted and returned. It is somewhat
similar to java's `Optional` class but much more powerful, it can extract anything from the response body.
If you find something missing feel free to contribute.<br/>

- `orNull()` returns `null` on any request error
- `orDefault(T)` returns the give default on any request error
- `orDefault(Supplier<T>)` preferred method when the default value is not a constant
- and many more

### Request parameters

To build the request parameter map we can use the [`RequestParameters`](../apiphany-core/src/main/java/org/apiphany/RequestParameters.java) class

```java
    public Info getInfo() {
        var params = RequestParameters.of(
                parameter("customerId", "myFirstCustomer")
        );
        return client()
                .get()
                .path("api", "v1", "info")
                .params(params)
                .retrieve(Info.class)
                .orDefault(Info::new);
    }
```

This will add the `customerId` request parameter and the request will be:<br/>
`http://awesome.somewhere.com/api/v1/info?customerId=myFirstCustomer` <br/>
You can add any number of request parameters, and they can be encoded too if the proper path method is called.

### Metrics

Metrics can be added in 2 ways (using the [`BasicMeters`](../apiphany-core/src/main/java/org/apiphany/meters/BasicMeters.java) class) 
1. for the whole client - all requests will use the same metrics
2. per request - each request has its own metrics

#### Metrics on all requests

To add metrics on all requests we can set them up in the client constructor:

```java
public class AwesomeClient extends ApiClient {

	public AwesomeClient(final ClientProperties properties) {
		super("http://awesome.somewhere.com",
				properties(properties)
						.secureWith()
						.oauth2());
		setMeters(BasicMeters.of("client.awesome"));
	}
}
```

This will add the following metrics on all client requests:
- `client.awesome.latency` request latency (timer)
- `client.awesome.request` request count (counter)
- `client.awesome.error` request errors (counter)

#### Metrics on specific request

Currently only Micrometer is supported as a metrics library, but feel free to contribute with other libraries if you want.

To add metrics only on some of the requests we can use `meters(String)` or `meters(BasicMeters)` method from [`ApiClientFluentAdapter`](../apiphany-core/src/main/java/org/apiphany/ApiClientFluentAdapter.java).

```java
    public Info getInfo() {
        return client()
                .get()
                .path("api", "v1", "info")
                .meters("client.awesome.info")
                .retrieve(Info.class)
                .orNull();
    }
```

This will add the following metrics on client `getInfo()` requests:
- `client.awesome.info.latency` request latency (timer)
- `client.awesome.info.request` request count (counter)
- `client.awesome.info.error` request errors (counter)

There is one more way to automatically generate the metrics with the client method name with `metersOnMethod(String)`:

```java
    public Info getInfo() {
        return client()
                .get()
                .path("api", "v1", "info")
                .metersOnMethod("client.awesome")
                .retrieve(Info.class)
                .orNull();
    }
```

This will add the following metrics on client `getInfo()`:
- `client.awesome.get-info.latency` request latency (timer)
- `client.awesome.get-info.request` request count (counter)
- `client.awesome.get-info.error` request errors (counter)

This method adds the method name transformed from Camel case to Kebab case (`getInfo` to `get-info`) to the specified
prefix given as parameter.

### Retries

This is the same as adding metrics but using the [`Retry`](../apiphany-core/src/main/java/org/apiphany/lang/retry/Retry.java) class.
1. for the whole client - all requests will use the same retry specification
2. per request - each request has its own retry specification

#### Example of retry on one request

```java
    public Info getInfo() {
        Retry retry = Retry.of(WaitCounter.of(3, Duration.ofSeconds(1)));
        return client()
                .get()
                .path("api", "v1", "info")
                .retry(retry)
                .retrieve(Info.class)
                .orNull();
    }
```

This will retry the request 3 times with a 1-second delay between retries. The retry will only take effect when there
are errors during the request.

If metrics are added to the request with retries, the retry attempts will be included in the metrics as well.
- `client.awesome.get-info.retry` request retries (counter)

### To be continued...
