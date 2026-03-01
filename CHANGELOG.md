## Release Notes

`1.1.6`

- Added `AbstractHttpExchangeClient.getHeaderValues` method that returns a list with all the values for a given header.
- Added `InputStreamSupplier` interface that supplies an `InputStream` used as a marker interface.
- Added `OneShotInputStreamSupplier` class that allows only a single `get` call, useful for retries.
- Added `OneShotHttpEntity` for Apache HTTP Client 5 clients that allows the input stream to be consumed only once.
- Added `ApacheHC5Entities.create(InputStream, ContentType)` similar to Apache `HttpEntities.create` methods but for handling input streams.
- Added `ApiClientFluentAdapter.body(Supplier)` to allow lazy body building.

---

`1.1.5`

- Added `LibraryDescriptor.of(String, Class)` to initialize a descriptor based on the presence in the classpath of the given class name.
- Added `LibraryDescriptor.present(Class)` to signal the presence of a library.
- Added `LibraryDescriptor.notPresent(Class)` to signal the absence of a library.
- Removed `LibraryDescriptor.of(boolean, Class)` because there are other more appropriate methods now.
- Upgraded `morphix-all` to `1.0.23`.
- Added `ClientProperties.defaults` to get an instance with default values.
- Changed `JsonBuilder.fromMap` to work without the need of a JSON library to be present in the classpath.
- Added `Lifecycle` enum to enumerate the life cycle options (`MANAGED` and `UNMANAGED`) for a `ScopedResource`.
- Changed `ScopedResource` to use the `Lifecycle` enum instead of using `boolean` parameters for methods and constructors.
- Updated `logback-classic` to `1.5.31`.
- Updated to JUnit 6 (`6.0.2`).
- Upgraded `oauth2-oidc-sdk` to `11.31.1`.
- Added `JsonBuilder.runtime` to get the current `JsonBuilder` instance.
- Added `JsonBuilder.with` method to run code on a different `JsonBuilder` with different settings.
- Added `JsonBuilder.toIdentityJsonString(Object)` to build the identity JSON in the given `JsonBuilder`.
- Added `Jackson2JsonBuilder.runtime` to get the current `Jackson2JsonBuilder` instance.
- Added `Jackson2JsonBuilder.with` method to run code on a different `Jackson2JsonBuilder` with different settings.
- Added `Jackson2JsonBuilder.getObjectMapper` to configure the underlying `ObjectMapper`.
- Added `Jackson2JsonBuilder(JsonFactory)` constructor to configure the `ObjectMapper` with a different factory since it can only be configured at construction.
- Added `Jackson2JsonBuilder.fromJsonInputStream` to de-serialize JSON objects from input streams.
- Extracted `ApiphanyJackson2Module` from `Jackson2JsonBuilder` for easier usage and added `instance` method.
- Added `Iterables` utility interface for `Iterable` and `Collection`.
- Added `Temporals.parseSimpleDuration` to parse durations like `1s`, `2m`, `3h` or `7d`. 
- Added `apache-httpclient5-tests` integration tests module.
- Added `Status.message` utility methods to build status messages.
- Added `ByteBufferSubscriber` from `apiphany-test` since it can be used generally and made it thread safe.
- Added `IOStreams.MAX_BUFFER_SIZE` to prevent `OutOfMemoryError` on I/O operations, all buffering operations use this value now. 

---

`1.1.4`

- Changed all request parameters to be `Map<String, List<String>>` instead of `Map<String, String>` to better accommodate HTTP specification.
- Renamed `RequestParameter.value` to `RequestParameter.toValues` to better emphasize what it does.
- Added `URIEncoder` class to be able to encode paths, query parameter names, query parameter values separately.
- Changed all request parameters from `HashMap` to `LinkedHashMap` because order can matter.
- Added `ApiClientFluentAdapter.param(Object, List, MultiValueStrategy)` method to specify the way the parameter should be encoded.
- Added `ParameterFunction.parameter(Object, List, MultiValueStrategy)` method to specify the way the parameter should be encoded.
- Added `Parameter.of(Object, List, MultiValueStrategy)` method to specify the way the parameter should be encoded.
- Renamed `ParameterFunction.none` to `ParameterFunction.ignored` to emphasize that parameter will be ignored.
- Added `openapi` package for OpenAPI specific functionality.
- Added `MultiValueStrategy` enumeration which describes how multi-value request parameters should be encoded.
- Added `ParameterStyle` enum for the style field of a Parameter Object as defined in the OpenAPI Specification.
- Added `ParameterMode` enum for the mode of the Parameter Object which can be `EXPLODE` or `JOINED`.
- Added `QueryParam` annotation to define the way fields behave in query parameter objects.
- Upgraded `morphix-all` to `1.0.21`.
- Removed `JavaArrays` since it's already available in `morphix-all`.

---

`1.1.3`

- Added support for multiple exchange client builders to be configured when extending `ApiClient`.
- Changed `OAuth2TokenProviderSpec` from record to class and added builder exclusive instantiation.
- Added `ResourceLocation` enumeration for specifying the location of the resource.
- Fixed `Strings.fromCamelToSnakeCase` to work on strings like `"FastXMLParser"` transforming it to `"fast_xml_parser"` or `"JSONValue"` to `"json_value"`.
- Fixed `Strings.fromPath` to work on absolute paths that do not start with `/`.
- Added a limit of `Integer.MAX_VALUE` to the maximum number of bytes the `Strings.fromPath` methods are allowed to read much like `InputStream.readAllBytes`.
- Added `Bytes.fromFile` to read a byte array from a given file.
- Renamed `ForkedLegacyHttpsServerRunner` to `ForkedHttpsServerRunner`.
- Changed `ForkedHttpsServerRunner` to run any HTTPS server that has a constructor like `MyHttpsServer(int, SSLProperties)` in a separate process so that it can have a separate SSL context.
- Updated `logback-classic` to `1.5.27`.
- Renamed package `json.jackson` to `json.jackson2` to better reflect the Jackson version used.
- Renamed all classes that use Jackson 2 library to have `Jackson2` in the name instead of `Jackson` only.
- Renamed `ApiphanyAnnotationIntrospector` to `ApiphanyJackson2AnnotationIntrospector`.
- Renamed `SensitiveAnnotationIntrospector` to `SensitiveJackson2AnnotationIntrospector`.
- Added `JavaArrays.isEmpty` to check if an array is `null` or empty.
- Added `LibraryDescriptor` class to describe a library support (like Jackson 2 or Micrometer).
- Added `LibraryInitializer` utility interface to create the proper instance for the available library in the classpath.
- Changed the way JSONs look when a JSON library is missing, the field is now named `identity` instead of `hash`.

---

`1.1.2`

- Added `ClientProperties.setBaseUrl` and `ClientProperties.getBaseUrl` to be able to configure the base URL per exchange client.
- Changed `ApiClient` to not allow construction without an `ExchangeClient` because that would cause an inconsistent state.
- Added `ExchangeClient.requireAuthenticationType` to check if an exchange client has the authentication type set.
- Renamed `ServerFinished` to `ServerFinishedEncrypted` to better describe the TLS object.
- Added `ClientFinishedEncrypted` for TLS client finished encrypted handshake.
- Renamed `Record.getHandshake` to `getHandshakeBody` where the return type is `TLSHandshakeBody`.
- Renamed `Record.hasHandshake` to `hasHandshakeBody`.
- Renamed `Record.hasNoHandshake` to `hasNoHandshakeBody`.
- Renamed `BulkCipher.algorithm` to `jcaKeyAlgorithm` to better emphasize its purpose.
- Added `BulkCipher.aeadCiphers` static method to return all AEAD ciphers.
- Added `BulkCipher.blockCiphers` static method to return all BLOCK ciphers.
- Added `BulkCipher.streamCiphers` static method to return all STREAM ciphers.
- Added `BulkCipher.encryptingCiphers` static method to return all encrypting ciphers.
- Added `BulkCipher.encryptingCiphers(CipherType)` static method to return all encrypting ciphers filtered by the given cipher type.
- Renamed `Record.hasHandshakeBody` method to `containsHandshakeBody`.
- Renamed `Record.hasNoHandshakeBody` method to `doesNotContainHandshakeBody`.

---

`1.1.1`

- Added Maven plugin management to parent `pom.xml` with default configurations - no executions in parent `pluginManagement`.
- Added `ClientCustomization` enum with two values `DEFAULT` and `NONE` to allow specific modes of client customizations and avoid using flags.
- Added `BulkCipherAlgorithm` enum with supported bulk cipher algorithms.
- Updated `CipherType` to reflect what it uses and what it provides via flags.
- Moved `MessageDigestAlgorithm.prfHmacAlgorithmName` to `PRF.algorithmName(MessageDigestAlgorithm)` because it is only used for TLS for now.
- Added missing `ServerFinished` TLS object to be used instead of a too generic `EncryptedHandshake` object.
- Upgraded `morphix-all` to `1.0.20`.

---

`1.1.0`

- Split the project into multiple modules.
- Added `apiphany-core` module which is the main library.
- Added `apiphany-test` module with testing utilities used in all modules.
- Added `apiphany-integration-test` module with integration tests.
- Added `apiphany-httpclient5` module which adds a library Apache HTTP client 5 for Apiphany.
- Upgraded `morphix-all` to `1.0.20`.
- Added `ApiResponse.getRequest` to return the `ApiRequest` that generated the `ApiResponse`.
- Added `Require.that(boolean, Function, String, Object...)` which provides a function to build the exception. 
- Added `Strings.fromCamelToSnakeCase` to transform a string from Camel case to Snake case.
- Added `Strings.fromLowerCamelToSnakeCase` which is just an alias for `fromCamelToSnakeCase`.
- Added `RequestParameters.from(Object, SimpleConverter)` to build request parameters from an object given the field name to parameter name converter.
- Added `ApiClientFluentAdapter.params(Object, SimpleConverter)` to build request parameters from an object given the field name to parameter name converter.
- Added `ApiClientFluentAdapter.param` to add a single request parameter.
- Moved `Tests` test class to `apiphany-test` module.
- Added `Assertions` class to `apiphany-test` module with utility JUnit style assertions.
- Added `Assertions.assertDefaultConstructorThrows` method to assert that the default constructor throws useful for utility classes.
- Added `org.apiphany.security.keys` package for key exchange handlers and other key related operations.
- Added `X25519Keys` key exchange handler to handle `XDH` keys.
- Moved `OAuth2ApiClientIT` integration test and `testcontainers` dependencies to `apiphany-integration-test` including relevant resource files.
- Added `Header.value` convenience static methods to build a header value which simply delegates to their equivalent `HeaderValues.value`.
- Moved `AbstractHttpExchangeClient.getTracingHeaders` method to `HttpExchangeClient` interface as a default method.
- Added `BasicHttpResponseParser` to easily parse HTTP response strings.
- Added `KeyStoreType` enum with the supported key store types.
- Added `Keys` utility interface with common key related methods.

---

`1.0.26`

- Upgraded `morphix-all` to `1.0.19`.
- Added `OAuth2TokenProviderSpec` record and builder to serve as an immutable configuration to build `OAuth2TokenProvider`s.
- Added constructor for `OAuth2TokenProvider` with `OAuth2TokenProviderSpec`.
- Removed all constructors from `OAuth2TokenProvider` except the one with `OAuth2TokenProviderSpec`.
- Added `OAuth2TokenProvider.of(OAuth2TokenProviderSpec)` factory method.
- Removed unnecessary `OAuth2Registry.tokenProvider` methods.
- Renamed `OAuth2TokenProviderOptions` to `OAuth2TokenProviderProperties`.
- Renamed `OAuth2TokenProvider.getOptions` to `OAuth2TokenProvider.getProperties`.
- Added `OAuth2TokenProviderProperties.Default.CLOSE_TASK_RETRY_INTERVAL` which is the interval between close attempts when closing the scheduled task.
- Added `OAuth2TokenProviderProperties.setCloseTaskRetryInterval` to set the interval between close attempts when closing the scheduled task.
- Added `OAuth2TokenProviderProperties.getCloseTaskRetryInterval` to retrieve the configured the interval between close attempts when closing the scheduled task.
- Added `OAuth2ExchangeClientBuilder.registrationName` to configure the client registration name.
- Changed `OAuth2HttpExchangeClient` to throw exception if no valid registration was found (multiple registrations exist and no client registration name given).
- Added `ExchangeClient.getCustomProperties` which delegates to `ClientProperties.getCustomProperties`.
- Added `OAuth2Properties.of` factory methods to construct OAuth2 properties objects.
- Changed `ExchangeClient.getHeadersAsString` to return a string easier to parse.
- Renamed `ExchangeClient.isRedactedHeader` to `ExchangeClient.isSensitiveHeader`.
- Added `HeaderValues.value(Object, Object, String)` with separator string between the l-value and r-value. 
- Added `ApiClient.with(ClientProperties)` static method to initialize the client with default `JavaNetHttpExchangeClient` and given properties.
- Changed visibility for `ApiClient.getExchangeClient(AuthenticationType)` from `protected` to `public`.
- Changed `SecuredExchangeClientBuilder` to throw exception if no security was configured.
- Changed `Accumulator.accumulate(Supplier, Object)` to `Accumulator.accumulate(Supplier, Supplier)` to avoid computation of the default value if not needed.
- Added `ApiMessage.getDisplayHeaders` which is used to retrieve the headers that can be displayed, logged, or serialized.
- Removed `ApiMessage.getHeadersAsString` because it is no longer needed, the callers can use the newly added `getDisplayHeaders` and transform it to string as needed.
- Added `ExchangeClient.getDisplayHeaders` which is used to retrieve the headers that can be displayed, logged, or serialized.
- Removed `ExchangeClient.getHeadersAsString` because it is no longer needed, the callers can use the newly added `getDisplayHeaders` and transform it to string as needed.

---

`1.0.25`

- Added new `OAuth2TokenProviderRegistry.of` factory method which allows filtering out the creation of `OAuth2TokenProvider`s.
- Added missing `removeLast` to `AlwaysEmptyList`.
- Changed `Lists.merge` to always return a new list even if any of the inputs is empty.
- Fixed `Lists.merge` to have O(n) complexity for single linked lists also.
- Added `Accumulator.addInformation` to easily add information to an accumulator.
- Added `ExcheptionsAccumulator.Throw` to configure the throwing mode of the accumulator when calling `rest` method instead of `boolean` flags.
- Changed `ExceptionsAccumulator` to use the `ExcheptionsAccumulator.Throw` to configure the throwing mode.
- Changed `Accumulator.EmptyAccumulator` to use `AlwaysEmptyList` instead of `Collections.emptyList`.
- Extracted `LoggingFunction` functional interface from `ExchangeLogger` class to `lang.function` package.

---

`1.0.24`

- Upgraded parent to `3.5.9`.
- Upgraded `exec-maven-plugin` to `3.6.3`.
- Upgraded `org.eclipse.jdt/org.eclipse.jdt.core` to `3.44.0`.
- Added support for XML via `jackson-dataformat-xml` only available if library is provided.
- Changed `Strings.fromFile` to be more deterministic, everything that starts with `/` is absolute, everything else relative.
- Upgraded formatter version to `4.38`.
- Upgraded `spotless-maven-plugin` to `3.1.0`.
- Added `code-format` version `1.0.0` as the source for Java formatter file `java-code-style.xml`.
- Renamed `Parameter` to `RequestParameter`.
- Added `Parameter` interface which can be used in pace of `ParameterFunction` (`ParameterFunction.parameter` -> `Parameter.of`).

---

`1.0.23`

- Moved `gzip` package to a better suited package `io.gzip`.
- Added `Hex.string(int, int)` to convert an integer to hexadecimal string with the given width.
- Added `jmh-core` version `1.37` for benchmarking.
- Added `jmh-generator-annprocess` version `1.37` for benchmarking.
- Added `DelimitedStringBuilder.of` factory method for easier building.
- Added `RequestParameters.from(Object)` factory method to create a request parameters map from a given object.
- Added parameters object support when building API requests (`ApiClientFluentAdapter.params(Object)`)
- Added `Assert.thatArgumentNot` which throws `IllegalArgumentException` if the condition is `true`.
- Added `RequestParameters.value` function to build a request parameter value from an object or object array.
- Renamed `Arrays` class to `JavaArrays` to avoid confusion with the Java `Arrays` class.
- Added `JavaArrays.toArray(Object)` to convert any object to an array for handling various input types uniformly.
- Upgraded `morphix-all` to `1.0.18`.
- Upgraded `nimbus-jose-jwt` to `10.7`.
- Upgraded `oauth2-oidc-sdk` to `11.31.1`.
- Upgraded `testcontainers` to `2.0.3`.
- Upgraded `testcontainers-keycloak` to `4.1.0`.
- Added `Strings.isBlank` to check if a character sequence is whitespace, `null` or empty.
- Added `Strings.isNotBlank` to check if a character sequence is not whitespace, `null` or empty.
- Added `Parameter` record to build (request) parameters.
- Updated `ParameterFunction` to use `Parameter.value` instead of `String.valueOf`.
- Renamed `Assert` to `Require` even if it is a verb rather than a noun because it conveys the action of requiring certain conditions to be met.
- Renamed `Assert.thatArgument` to `Require.that`.
- Renamed `Assert.thatArgumentNot` to `Require.thatNot`.
- Added `Require.notNull` to check for `null` values and to throw `IllegalArgumentException` instead of `NullPointerException`.
- Added `Wait.Default` name space class which holds the defaults for `Wait` objects such as sleep action, interval, time unit.
- Added `Wait.interval` method which defaults to `Wait.Default.INTERVAL` with value `1`.
- Added `Wait.timeUnit` method which defaults to `Wait.Default.TIME_UNIT` with value `TimeUnit.SECONDS`.
- Added `Wait.sleepAction` method which defaults to `Wait.Default.SLEEP_ACTION` which uses `Threads.safeSleep`.

---

`1.0.22`

- Added `Headers.of` method to dynamically build a headers map.
- Added `HeaderFunction` functional interface as a helper to dynamically build a headers map together with `Headers.of`.
- Added `Header` utility interface with factory methods to dynamically build headers.
- Changed the whole `ExchangeClient` hierarchy to favor composition instead of inheritance.
- Added `ExchangeClient.getCommonHeaders` to provide headers that will be added to all requests.
- Added `ExchangeClient.getTracingHeaders` to provide tracing headers that will be added to all requests.
- Removed `AbstractHttpExchangeClient.addTracingHeaders` in favor of `ExchangeClient.getTracingHeaders`.
- Added `DelegatingExchangeClient` interface to signify a client that delegates all functionality to an underlying exchange client.
- Removed `DecoratingHttpExchangeClient` in favor of `DecoratingExchangeClient` to better fit its functionality.
- Removed `AbstractAuthenticatedHttpExchangeClient` in favor of `AuthenticatedExchangeClient` interface.
- Changed `AbstractAuthorizedHttpExchangeClient` to an interface and renamed it to `AuthorizedHttpExchangeClient`.
- Added `ScopedResource.closeIfManaged(Consumer)` which can handle resource closing exceptions.
- Renamed `ScopedResource.checked` method to `ensureSingleManager` to better emphasize what it does.
- Added constructor with resource only to `ScopedResource` defaulting to a managed resource.
- Fixed `HttpContentType.parse` methods when `charset` parameter is missing but other parameter is present.
- Renamed `ExchangeClientBuilder.decorateWith` with builder class parameter to `ExchangeClientBuilder.decoratedWithBuilder`.
- Added `ExchangeClientBuilder.decoratedWith` with `DecoratingExchangeClient` class as parameter.
- Renamed `ExchangeClientBuilder.secureWith` with builder class parameter to `ExchangeClientBuilder.securedWith`.
- Added `ApiClient.with(ExchangeClient)` static method to be used when the exchange client is managed by the caller.
- Renamed `ApiClient.exchangeClient` static method to `ApiClient.withClient`.
- Added `HttpContentType.normalizedValue` method to return the value in RFC 7231-style.
- Fixed `ApiPredicates.hasResponse` to return `true` only when the response is not `null`.
- Added `ApiMessage.addHeaders` to add headers to an existing message.

---

`1.0.21`

- Upgraded parent to `3.5.8`.
- Upgraded `testcontainers-keycloak` to `4.0.1`.
- Added a new constructor to `ApiClient` with the base URL which uses the `JavaNetHttpExchangeClient` as the default client.
- Added a new constructors to `ApiClient` with `ScopedResource` for exchange client.
- Added `DecoratingHttpExchangeClient` as a base class for all authenticated exchange clients since all are decorators.
- Added `AuthenticatedExchangeClient` interface to signify authenticated exchange clients.
- Added `AbstractAuthenticatedHttpExchangeClient` which extends `DecoratingHttpExchangeClient` to streamline the exchange and authentication.
- Added `AbstractAuthenticatedHttpExchangeClient.authenticate` method to add all necessary authentication information to an API request.
- Added `AuthorizationHeaderProvider` functional interface for supplying the value of the Authorization header.
- Added `AbstractAuthorizationHttpExchangeClient` which automatically adds the provided `Authorization` header value.
- Changed `TokenHttpExchangeClient` to inherit from `AbstractAuthorizationHttpExchangeClient`.
- Added `AuthenticationType.API_KEY` for API key based authentication.
- Renamed `AuthenticationTokenClientSupplier` to `OAuth2TokenClientSupplier` since it is OAuth2 specific.
- Added `JacksonLibrary` class with information about the Jackson JSON library.
- Added `MicrometerLibrary` class with information about the Micrometer library.
- Renamed `OAuth2ExchangeClientBuilder` to `OAuth2HttpExchangeClientBuilder`.
- Added `ExchangeClientBuilder.decorateWith` to decorate an existing exchange client.
- Added `SecuredExchangeClientBuilder` to add security to an existing exchange client.
- Added `ContentEncoding.parse` to build a `ContentEncoding` based on a list of values, the first matching being returned.
- Added `ContentEncoding.fromString` with default value supplier for building without exception handling.
- Renamed `GZip.decompress` methods which return `String` to `decompressToString`.
- Added `GZip.decompressToBytes` method to de-compress a byte array to a byte array.
- Added `GZip.decompress` method which handles both compressed byte array and compressed `InputStream` to de-compress to the same type as the input.
- Added `ContentConverter.decodeBody` method to decode a given body and a `ContentEncoding`. 
- Changed request/response debug logging information order.
- Renamed all parsing methods in `HttpContentType` to `parse` with different parameter types.

---

`1.0.20`

- Moved default values from `Sockets` class into `Sockets.Default` name space inner class.
- Added `AuthenticationTokenClientSupplier` functional interface that provides a token client instead of using a generic `BiFunction`.
- Added `OAuth2Registry.of` method without parameters to create an empty `OAuth2Registry`.
- Changed `OAuth2TokenProvider` to throw `AuthenticationException` if the token client returns a `null` token.
- Changed `OAuth2TokenProvider` to throw `AuthenticationException` if the token client returns a token with invalid `expires_in` field.
- Added `OAuth2TokenProviderRegistry.getProvider` method to return a registered provider by name.
- Added variations of the `OAuth2TokenProviderRegistry.of` method without the token provider name function.
- Added `AlwaysEmptyList` class that represents a list that is always empty no matter what operations are called on it.
- Added `ErrorObservingInputStream` as a wrapper around an `InputStream` that observes errors.
- Renamed `BulkCipher._3DES_EDE_CBC` to `BulkCipher.TRIPLE_DES_EDE_CBC` to match Java constant naming conventions.

---

`1.0.19`

- Added `OAuth2TokenProviderRegistry` that holds all `OAuthTokenProvider` instances.
- Added `maven-properties-plugin` to `pom.xml` to output all Maven properties to `target/maven.properties`.
- Added `JacksonJsonBuilder.configureSensitivity` method to configure `Sensitive` annotations behavior.

---

`1.0.18`

- Fixed random generation for `BulkCipher`.
- Changed `JsonBuilder.toDebugString` to return `Class.getCannonicalName` instead of `Class.getSimpleName`.
- Upgraded `morphix-all` to `1.0.14`.
- Added `Temporals.formatToSeconds(double, Locale)` so that a `Locale` can also be specified when formatting. 
- Added class file version test to ensure correct class files are built.

---

`1.0.17`

- Upgraded `testcontainers-keycloak` to `4.0.0`.
- Upgraded `nimbus-jose-jwt` to `10.6`.
- Upgraded `oauth2-oidc-sdk` to `11.30.1`.
- Upgraded `pitest-maven` to `1.22.0`.
- Upgraded `jacoco-maven-plugin` to `0.8.14`.
- Upgraded `maven-gpg-plugin` to `3.2.8`.
- Upgraded `morphix-all` to `1.0.13`.

---

`1.0.16`

- Renamed all `Retry.when` methods to `Retry.until` to better emphasize what the parameters actually do.
- Renamed `Accumulator.emptyAccumulator` method to `Accumulator.empty` for brevity.
- Changed `Retry.until` methods to always have the first 2 parameters the result supplier first and the exit condition second where applicable.
- Renamed `Retry.fluent` method to `Retry.policy` to better match its purpose.
- Upgraded `testcontainers` to `2.0.2`.
- Removed all JUnit4 dependencies.
- Removed `HttpHeaderValuesChain` in favor of `HttpHeaderValues` to hold the chain.
- Added `MapHeaderValues.contains` method specific to headers as map.
- Added `HttpHeaderValues.contains` method specific to headers as `HttpHeaders`.
- Changed `AbstractHttpExchangeClient.getHeaderValues` to return `HeaderValues` instead of `HeaderValuesChain`.
- Changed `AbstractHttpExchangeClient.addDefaultHeaderValues` to use `HeaderValues` instead of `HeaderValuesChain`.
- Changed `AbstractHttpExchangeClient.addDefaultHeaderValues` to return the first `HeaderValues` in the chain.
- Changed `ContentConverter.getHeaderValues` and its implementations to use `HeaderValues` instead of `HeaderValuesChain`.
- Moved all map header method implementations to `Headers` and `MapHeaderValues` now delegates to them.
- Renamed `HttpStatus.getMessage` to `HttpStatus.message` for consistency with other methods.
- Renamed `HttpStatus.from` to `HttpStatus.fromCode` to better emphasize what the method does.
- Added `HttpStatus.fromString` to build an HTTP status from a `String`.
- Added `HttpStatus.fromMessage` to build an HTTP status from an HTTP `String` status message.
- Renamed `HttpStatus.Type.from` to `HttpStatus.Type.fromCode` to better emphasize what the method does.
- Changed `ContentConverter.getHeaderValues` parameter order, now the header name is the first parameter.

---

`1.0.15`

- Added `ContentType.Value` name space class so that all content type string values are accessible directly.
- Upgraded `morphix-all` to `1.0.11`.
- Added `OAuth2ErrorResponse` for the standard OAuth2 error responses.
- Added `OAuth2ErrorCode` for the standard OAuth2 error response error codes.
- Added `Creator` annotation to signify the constructor or a factory method to use for object creation, similar to `JsonCreator` from Jackson.

---

`1.0.14`

- Added overloads for `Sockets.findAvailableTcpPort`.
- Added `Assert` utility class to be used to check preconditions.
- Added `Bytes.padRightToBlockSize` to add padding to a byte array given a block size.
- Added `Bytes.padPKCS7` to add PKCS#7 padding to a byte array.
- Renamed `OAuth2TokenProviderConfiguration` to `OAuth2TokenProviderOptions` so it's not confused with configuration objects in Spring environments.
- Added `OAuth2Parameter.CLIENT_ASSERTION` that represents client assertion parameter used in JWT/SAML client authentication flows.
- Added `OAuth2Parameter.CLIENT_ASSERTION_TYPE` that specifies the assertion type for client authentication.
- Added `JcaSignatureAlgorithm` enumeration of standard JCA (Java Cryptography Architecture) signature algorithm names.
- Added `JwsAlgorithm` enumeration of standard JWS (JSON Web Signature) algorithms.
- Added `SignatureAlgorithm.jcaSignature` to extract the JCA signature for a TLS signature algorithm.
- Added `Signer` utility class to easily sign and verify with a private and public key.
- Renamed `IO` class to `IOStreams` to not create confusion because Java 25 will add an `IO` class.
- Changed `OAuth2ApiClient` constructors to use `JwsAlgorithm` instead of `String`.

---

`1.0.13`

- Added `Strings.fromCamelToKebabCase` as an alias for `Strings.fromLowerCamelToKebabCase`.
- Added `Strings.fromKebabToCamelCase` as an alias for `Strings.fromKebabToLowerCamelCase`.
- Added `Strings.fromFile(String, Consumer)` to handle file reading errors.
- Renamed `Hex.string(byte[])` to `Hex.spacedString(byte[])`.
- Added `Hex.string(byte[])` that converts a byte array to hexadecimal `String`.
- Added `Hex.stringSupplier(byte[])` that supplies the hexadecimal string representation of the given byte array.
- Fixed `RequestParameters.from` to properly decode the key and value.
- Upgraded `pitest-maven` to `1.20.3`.
- Upgraded `maven-gpg-plugin` to `3.2.8`.
- Upgraded `central-publishing-maven-plugin` to `0.9.0`.
- Upgraded `oauth2-oidc-sdk` to `11.29.1`.

---

`1.0.12`

- Fixed `MicrometerCounter` and `MicrometerTimer` `unwrap` methods to properly validate the wanted class.
- Renamed `CipherSuite.getMessageDigest` method to `messageDigest` to match other method names in the enumeration.
- Added `MessageDigestAlgorithm.digestLength` that returns the digest length in bytes.
- Added `MessageDigestAlgorithm.hmac` to compute the HMAC for a given key and data.
- Added `MessageDigestAlgorithm.prfHmacAlgorithmName` to retrieve the correct TLS 1.2 PRF algorithm for the algorithm.
- Added `MessageDigestAlgorithm.digest(byte[], String)` static method to compute the hash for a given data and a given hash algorithm.
- Added `MessageDigestAlgorithm.sanitizedValue` method to sanitize the hashing algorithm name since Java handles `SHA-1` as `SHA-256` for example.
- Added `MessageDigestAlgorithm.sanitizedDigest` to compute the hash with the sanitized hash algorithm.
- Added `BulkCipher` enumeration that represents the bulk (symmetric) encryption algorithm used in a TLS cipher suite.
- Added `BulkCipherInfo` record that holds all the information needed to properly construct a `BulkCipher`.
- Added `CipherSuite.bulkCipher` to associate the `BulkCipher` with a `CipherSuite`.
- Added `CipherType` to represent the cipher types like AEAD, BLOCK, etc.
- Added `ExchangeKeys` which represents the set of keys derived from the TLS key block for a given cipher suite.
- Added `SSLProtocol.TLS_1_2_MASTER_SECRET_LENGTH` constant with the TLS 1.2 master secret length in bytes (48).
- Added `maven_deploy.sh` shell script to deploy to Maven Central which uses all the test parameters.
- Added `Bytes.isEmpty` to check if a byte array is empty.
- Added `Bytes.isNotEmpty` to check if a byte array is not empty.
- Added `BytesWrapper(byte[], int, int)` constructor to construct a wrapper over a slice of the byte array.
- Removed the nonce from `Encrypted` and changed the getters to get the nonce depending on the `BulkCipher`.

---

`1.0.11`

- Updated `testcontainers-keycloak` to `3.8.0`.
- Updated `nimbus-jose-jwt` to `10.5`.
- Updated `oauth2-oidc-sdk` to `11.28`.
- Updated MorphiX to `1.0.9`.
- Renamed `HttpException.statusMessage` static method to `HttpException.message`.
- Renamed `HttpException.exceptionMessage` static method to `HttpException.message`.
- Renamed `IO.bytesNeededEOFException` static method to `IO.eofExceptionBytesNeeded`.
- Added missing `equals` and `hashCode` to `UInt64` class.
- Added `UItn64.toUnsignedBigInteger` to get the 64 bit unsigned value.
- Renamed `UInt64.getValue` method to `UInt64.getSignedValue`.
- Added `Strings.stripChar` method that strips the given `String` by the given `char` from the beginning and the end.
- Removed references to Micrometer `Tags` in `ApiClientFluentAdapter`.
- Changed the `ApiClientFluentAdapter.url(String, String...)` method to correctly handle extra `/` characters in path segments.
- Added support for `MicrometerFactory.toTags` to convert `Collection` of objects to Micrometer `Tags`.
- Fixed `BytesWrapper` constructor to properly handle `Bytes.EMPTY`.

---

`1.0.10`

- Changed `AccumulatorException.toString` to be simpler.
- Added warning if multiple registrations are present in `OAuth2Properties` when creating an `OAuth2TokenProvider`.
- Added `OAuth2TokenProvider` constructor without client registration name.
- Added `OAuth2ResolvedRegistration` which holds a complete OAuth2 registration.
- Added `OAuth2Registry` to build a registry from an `OAuth2Properties` with all registrations configured into all `OAuth2ResolvedRegistration`s.
- Added `Temporals.formatToSeconds` to format a `double` to seconds as `String`.
- Changed `DurationAccumulator` to be faster and consume less memory.
- Changed `Headers.contains` with name, value and get values function to use `String.equalsIgnoreCase` instead of `String.contains`.
- Added `Headers.contains` with value comparing `BiPredicate`.
- Added `Strings.fromKebabToLowerCamelCase` to convert a Kebab string to a lower camel case string.

---

`1.0.9`

- Added `OAuth2TokenProviderConfiguration` with configurable properties for `OAuth2TokenProvider`.
- Added `OAuth2TokenProviderConfiguration.Default` with the default values used when instantiating the configuration.
- Added `OAuth2TokenProviderConfiguration.defaults` method to create an instance with default values.
- Added constructor with `OAuth2TokenProviderConfiguration` to `OAuth2TokenProvider`.
- Added `OAuth2TokenProvider.getConfiguration` method to be able to change the configuration dynamically.
- Removed `BytesWrapper.toHexString` method and moved the implementation to `toString` method since JSON doesn't make sense here.
- Removed `ExchangeRandom.toHexString` method and moved the implementation to `toString` method since JSON doesn't make sense here.
- Added `IO.bytesNeededEOFException` to uniformly build exceptions for the `IO` utility class.
- Updated to MorphiX to `v1.0.8`.

---

`1.0.8`

Changed `AuthenticationException` to extend `SecurityException`.

---

`1.0.7`

- Added `PRF` with utility methods to apply pseudo-random function in TLS environments.
- Added `KeyExchangeHandler` interface to be implemented in key exchanges.
- Added `Sensitive` annotation to mark sensitive fields.
- Changed `JacksonJsonBuilder` to read sensitive but not write `Sensitive` fields this is useful for passwords fields.
- Added `@Ignored` annotation which is replacing Jackson's `@JsonIgnore` and is JSON library agnostic.
- Added `@FieldName` annotation which is replacing Jackson's `@JsonProperty` and is JSON library agnostic.
- Added `@FieldOrder` annotation which is replacing Jackson's `@JsonPropertyOrder` and is JSON library agnostic.
- Added `@AsValue` annotation which is replacing Jackson's `@JsonValue` and is JSON library agnostic.
- Added `ApiphanyAnnotationIntrospector` which handles the newly added annotations when Jackson is present in the classpath so that the JSON library is abstracted away.
- Added `@Sensitive` annotation for sensitive fields that will be deserialized but not serialized for security.
- Added `SensitiveAnnotationIntrospector` that handles the `@Sensitive` annotation.
- Changed `BasicMeters` class so that is metric library agnostic.
- Added `Meter` base interface for meters.
- Added `MeterTimer` interface for timer meters.
- Added `MeterCounter` interface for counter meters.
- Added `MeterFactory` which will create meter library agnostic meters.
- Added Micrometer implementation which will only be used if Micrometer library is present in the classpath.
- Added `MicrometerTimer` which wraps a Micrometer `Timer`.
- Added `MicrometerCounter` which wraps a Micrometer `Counter`.
- Added `MicrometerFactory` which creates a Micrometer meters (only if Micrometer library is present in the classpath).
- Removed the Micrometer library from the build.

---

`1.0.6`

- Added `ApiMimeType` as an interface to represent a mime type.
- Added `ApiMimeType.parseCharset` to parse a `Charset`.
- Moved `ContentType` from `http` package to `io` package.
- Changed `ContentType` to implement `ApiMimeType`.
- Renamed `ResolvedContentType` to `HttpContentType` to better match its purpose.
- Added `AuthenticationTokenProvider` interface to declare authentication token providers.
- Changed `OAuth2ApiClient` to respect the `AuthenticationTokenProvider` contract to throw `AuthenticationException` when token retrieval fails.
- Added `ApiResponse.orThrow` to throw an exception if the request fails.
- Added `ApiResponse.orRethrow` to re-throw the exception wrapped into another exception via an exception wrapping function.
- Added `ScopedResource.isNotManaged` method which returns true if the underlying resource is not managed.
- Added `ScopedResource.checked` method to check for the same resource being managed twice.
- Changed `OAuth2HttpExchangeClient` to properly handle managed and unmanaged resources.
- Added `OAuth2TokenProvider` to handle OAuth2 tokens.
- Added `AuthenticationToken.EXPIRATION_ERROR_MARGIN` for token expiration checking.

---

`1.0.5`

- Fixed `HttpExchangeClient.head` method.
- Renamed `ExchangeClient.getRedactedHeaderPredicate` to `ExchangeClient.isRedactedHeader`.
- Added `ResolvedContentType` for content types resolved from API responses.
- Changed the default body handler for responses in `JavaNetHttpExchangeClient` from `String` to `byte[]`.
- Changed some `ContentConverter` methods to include the `ResolvedContentType`.
- Changed the `JacksonJsonBuilder.fromJson` to accept generic types so it can parse both `String` and `byte[]`.
- Added methods for working with headers as maps to `Headers` class.

---

`1.0.4`

- Added `PRFLabel` enum representing labels used in TLS Pseudo-random Function (PRF) operations.
- Added `MessageDigestAlgorithm` representing supported message digest algorithms for cryptographic operations.
- Added the message digest algorithm to each `CipherSuite` enum value.
- Added `Record.hasNoHandshake` method to check of a TLS record doesn't have the handshake message type specified as a parameter.
- Moved `ByteSizeable` and `BinaryRepresentable` to `io` package.
- Added `BytesOrder` enum and streamlined key handling.
- Added `Sockets.DEFAULT_TIMEOUT` set to 2 seconds.
- Added methods without timeout specifier to `Sockets` which use the `Sockets.DEFAULT_TIMEOUT`.
- Added `ScopedResource` class for managed/unmanaged resource handling.
- Added `LoggingFormat` enum to specify logging format configurations.
- Added `TLSObject.serialize` to serialize any TLS object to string depending on configuration.
- Added `TLSObject.FORMAT` as the logging format configurable with `apiphany.logging.format.tls` property.
- Moved all OAuth2 model classes to the same package `oauth2` since they can be used by both the server or the client despite their names.
- Added support for TLS fragmented handshake records.
- Added `RawHandshakeBody` to be able to build/read raw handshake messages.
- Added `TLSEncryptedObject` abstract class and derived TLS classes to denote any TLS message that is encrypted.
- Added `IO` class with input/output stream operations.
- Moved `DEFAULT_BUFFER_SIZE` constant from `Strings` to `IO`.

---

`1.0.3`

- Added `Arrays` class for Java array utility methods.
- Added `Strings.envelope` method to add a string as both a prefix and a suffix to a given string.
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
- Added `UInt8`, `UInt16`, `UInt24`, `UInt32`, `UInt64` for 8, 16, 24, 32 and 64 bit unsigned integers.
- Added first version of TLS model classes to the `*.security.tls` package and a working minimal TLS client in the test packages.
- Added `BasicMeters` caching for fewer objects creation (this only caches meters without tags).
- Added `BasicMeters.isEmpty(Tags)` to efficiently check if tags are empty or not.
- Added `ByteBufferInputStream` as an alternative to `ByteArrayInputStream` without synchronization overhead.

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
- Added `OAuth2ExchangeClientBuilder` to build exchange clients with built-in OAuth2 functionality.
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


