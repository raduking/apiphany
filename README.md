# Apiphany

Simple fluent style Java API to make API calls.

### Languages and Tools

<p>
	<a href="https://www.java.com" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="java" width="40" height="40"/></a>
	<a href="https://micrometer.io" target="_blank" rel="noreferrer"><img src="https://micrometer.io/img/logo-no-title.svg" alt="micrometer" width="40" height="40"/></a>
</p>

### Why APIphany?

This is a simple wrapper over the `java.net.http` library to be able to use a fluent style API, to make API calls in a very easy way.

### License

[Apache License, Version 2.0](LICENSE)

### Getting Started

Maven: add this dependency to your `pom.xml`

```xml
<dependency>
    <groupId>io.github.raduking</groupId>
    <artifactId>apiphany</artifactId>
    <version>1.0.0</version>
</dependency>
```

### How to use

To make a simple HTTP GET request to `http://my.awesome.domain/api/info`:

```java
import org.apiphany.ApiClient;
import org.apiphany.client.HttpExchangeClient;

public class MyCustomerClient extends ApiClient {

    public MyCustomerClient() {
        super("http://my.awesome.domain", new HttpExchangeClient());
    }

    public String getInfo() {
        return client()
                .get()
                .path("api", "info")
                .retrieve(String.class)
                .orNull();
    }
}
```

```java
void main() {
    MyCustomerClient client = new MyCustomerClient();

    String info = client.getInfo();

    System.out.println("Info: " + info);
}
```
