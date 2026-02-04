# Apiphany

Simple fluent style Java API to make API calls.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.raduking/apiphany)](https://central.sonatype.com/artifact/io.github.raduking/apiphany)
[![GitHub Release](https://img.shields.io/github/v/release/raduking/apiphany)](https://github.com/raduking/apiphany/releases)
[![License](https://img.shields.io/github/license/raduking/apiphany)](https://opensource.org/license/apache-2-0)
[![Java](https://img.shields.io/badge/Java-21+-blue)](https://www.oracle.com/java/technologies/downloads/#java21)
[![PRs](https://img.shields.io/github/issues-pr/raduking/apiphany)](https://github.com/raduking/apiphany/pulls)

#### Status

[![branch: master](https://img.shields.io/badge/branch-master-blue)](https://github.com/raduking/apiphany/tree/master)
![Build (master)](https://github.com/raduking/apiphany/actions/workflows/build.yml/badge.svg?branch=master)
[![branch: develop](https://img.shields.io/badge/branch-develop-purple)](https://github.com/raduking/apiphany/tree/develop)
![Build (develop)](https://github.com/raduking/apiphany/actions/workflows/build.yml/badge.svg?branch=develop)

### Languages and Tools

<p>
	<a href="https://www.java.com" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="java" width="40" height="40"/></a>
	<a href="https://maven.apache.org/" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/maven/maven-original.svg" alt="maven" width="40" height="40"/></a>
	<a href="https://junit.org/" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/junit/junit-plain.svg" alt="junit" width="40" height="40"/></a>
	<a href="https://git-scm.com/" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/git/git-original.svg" alt="git" width="40" height="40"/></a>
	<a href="https://github.com/features/actions" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/github/github-original.svg" alt="github actions" width="40" height="40"/></a>
</p>

### Why Apiphany?

- Because you want to change the underlying HTTP client by changing one line in your client and one dependency.
- Because you want to be able to use a fluent style API to make API calls in a very easy way.
- Because you want to be able to mock API calls in your tests without using external libraries.

Currently, it wraps over the `java.net.http` library but you can easily extend it to use other HTTP clients by implementing the 
`HttpExchangeClient` interface or by extending the `AbstractHttpExchangeClient`.

Support is being developed for other HTTP clients like Apache HttpClient, OkHttp, Spring RestTemplate, etc.

### License

[Apache License, Version 2.0](LICENSE)

### Releases

Current release `1.1.3`

### Getting Started

Maven: add this dependency to your `pom.xml`

```xml
<dependency>
    <groupId>io.github.raduking</groupId>
    <artifactId>apiphany</artifactId>
    <version>1.1.3</version>
</dependency>
```

You can also use the latest development build, but you need to build it yourself.

```
git clone git@github.com:raduking/apiphany.git
cd apiphany
git checkout develop
mvn clean install
```

The project being in active development. Expect class name changes, method name changes, and other similar changes between versions.
All such changes are documented in [CHANGELOG.md](CHANGELOG.md).

### How to use

To make a simple HTTP GET request to `http://my.awesome.domain/api/info`:

```java
import org.apiphany.ApiClient;

public class MyAwesomeClient extends ApiClient {

    public MyAwesomeClient() {
        super("http://my.awesome.domain");
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

See the [docs](docs) directory for project specifications, architecture notes, and user guides.
