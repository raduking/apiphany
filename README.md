# Apiphany

Simple fluent style Java API to make API calls.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.raduking/apiphany)](https://central.sonatype.com/artifact/io.github.raduking/morphix-all)
[![GitHub Release](https://img.shields.io/github/v/release/raduking/apiphany)](https://github.com/raduking/apiphany/releases)
[![License](https://img.shields.io/github/license/raduking/apiphany)](https://opensource.org/license/apache-2-0)
[![Java](https://img.shields.io/badge/Java-21+-blue)](https://www.oracle.com/java/technologies/downloads/#java21)
[![PRs](https://img.shields.io/github/issues-pr/raduking/apiphany)](https://github.com/raduking/apiphany/pulls)

### Languages and Tools

<p>
	<a href="https://www.java.com" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="java" width="40" height="40"/></a>
</p>

### Why APIphany?

This is a simple wrapper over the `java.net.http` library to be able to use a fluent style API, to make API calls in a very easy way.

### License

[Apache License, Version 2.0](LICENSE)

### Releases

Current release `1.0.11`

### Getting Started

Maven: add this dependency to your `pom.xml`

```xml
<dependency>
    <groupId>io.github.raduking</groupId>
    <artifactId>apiphany</artifactId>
    <version>1.0.11</version>
</dependency>
```

You can also use the latest development build but you need to build it yourself.

```
git clone git@github.com:raduking/apiphany.git
cd apiphany
git checkout develop
mvn clean install
```

### How to use

To make a simple HTTP GET request to `http://my.awesome.domain/api/info`:

```java
import org.apiphany.ApiClient;
import org.apiphany.client.JavaNetHttpExchangeClient;

public class MyAwesomeClient extends ApiClient {

    public MyAwesomeClient() {
        super("http://my.awesome.domain", with(JavaNetHttpExchangeClient.class));
    }

    public String getInfo() {
        return client()
                .http()
                .get()
                .path("api", "info")
                .retrieve(String.class)
                .orNull();
    }
}
```

```java
void main() {
    MyAwesomeClient client = new MyAwesomeClient();

    String info = client.getInfo();

    System.out.println("Info: " + info);
}
```

## Documentation

See the [docs](docs/) directory for project specifications, architecture notes, and user guides.
