package org.apiphany.client.http;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientCustomization;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ClientProperties.Timeout;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.InputStreamSupplier;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.apiphany.lang.collections.Maps;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;

/**
 * HTTP exchange client based on the default Java {@link HttpClient}. The default HTTP version used is HTTP/1.1.
 *
 * @author Radu Sebastian LAZIN
 */
public class JavaNetHttpExchangeClient extends AbstractHttpExchangeClient {

	/**
	 * Underlying Java net HTTP client.
	 */
	private final HttpClient httpClient;

	/**
	 * Default constructor will initialize the client with default client properties. See the {@link ClientProperties} class
	 * to see the defaults.
	 */
	public JavaNetHttpExchangeClient() {
		this(ClientProperties.defaults());
	}

	/**
	 * Initialize the client with the given client properties.
	 *
	 * @param clientProperties client properties
	 */
	public JavaNetHttpExchangeClient(final ClientProperties clientProperties) {
		super(clientProperties);
		this.httpClient = createClient(this::customize);
	}

	/**
	 * Initialize the client from the given client properties that reside at the given path built from the given prefix and
	 * client name.
	 *
	 * @param clientProperties client properties
	 * @param prefix prefix for the path to the actual client properties
	 * @param clientName client name
	 */
	public JavaNetHttpExchangeClient(final ClientProperties clientProperties, final String prefix, final String clientName) {
		this(clientProperties.getClientProperties(prefix, clientName));
	}

	/**
	 * Initialize the client with the given underlying HTTP client.
	 * <p>
	 * This constructor can be used when more advanced customization of the underlying HTTP client is needed but only by
	 * deriving from this class.
	 *
	 * @param clientProperties client properties
	 * @param httpClient underlying HTTP client
	 */
	protected JavaNetHttpExchangeClient(final ClientProperties clientProperties, final HttpClient httpClient) {
		super(clientProperties);
		this.httpClient = httpClient;
	}

	/**
	 * Initialize the client with the given HTTP client builder.
	 * <p>
	 * This constructor can be used when more advanced customization of the underlying HTTP client is needed but only by
	 * deriving from this class.
	 *
	 * @param clientProperties client properties
	 * @param httpClientBuilder HTTP client builder
	 * @param clientCustomization whether to apply the default customization or not
	 */
	protected JavaNetHttpExchangeClient(final ClientProperties clientProperties, final HttpClient.Builder httpClientBuilder,
			final ClientCustomization clientCustomization) {
		super(clientProperties);
		this.httpClient = switch (clientCustomization) {
			case DEFAULT -> customize(httpClientBuilder).build();
			case NONE -> httpClientBuilder.build();
		};
	}

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		httpClient.close();
	}

	/**
	 * Creates the underlying HTTP client.
	 *
	 * @param httpClientBuilderCustomizer client builder customizer
	 * @return a new HTTP client
	 */
	private static HttpClient createClient(final Consumer<HttpClient.Builder> httpClientBuilderCustomizer) {
		HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
		httpClientBuilderCustomizer.accept(httpClientBuilder);
		return httpClientBuilder.build();
	}

	/**
	 * Customizes the HTTP client builder.
	 *
	 * @param httpClientBuilder HTTP client builder
	 * @return the customized HTTP client builder
	 */
	private HttpClient.Builder customize(final HttpClient.Builder httpClientBuilder) {
		ClientProperties properties = getClientProperties();
		JavaNetHttpProperties httpProperties = getCustomProperties(JavaNetHttpProperties.class);

		// HTTP version
		HttpClient.Version version = Nullables.notNull(httpProperties)
				.andNotNull(JavaNetHttpProperties::getRequest)
				.thenNotNull(JavaNetHttpProperties.Request::getHttpVersion)
				.orElse(() -> JavaNetHttpProperties.Request.DEFAULT_HTTP_VERSION);
		httpClientBuilder.version(version);

		// SSL context
		Nullables.notNull(getSslContext()).then(httpClientBuilder::sslContext);

		// Timeouts
		Nullables.notNull(getUsableTimeout(properties.getTimeout(), Timeout::getConnect))
				.then(httpClientBuilder::connectTimeout);
		return httpClientBuilder;
	}

	/**
	 * @see ExchangeClient#exchange(ApiRequest)
	 */
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		apiRequest.addHeaders(getCommonHeaders());
		apiRequest.addHeaders(getTracingHeaders());

		HttpRequest httpRequest = buildRequest(apiRequest);
		HttpResponse<?> httpResponse = sendRequest(apiRequest, httpRequest);
		return buildResponse(apiRequest, httpResponse);
	}

	/**
	 * Sends the given HTTP request and returns the HTTP response.
	 *
	 * @param <T> request body type
	 * @param <R> HTTP response body type
	 *
	 * @param apiRequest API request
	 * @param httpRequest HTTP request
	 * @return HTTP response
	 */
	protected <T, R> HttpResponse<R> sendRequest(final ApiRequest<T> apiRequest, final HttpRequest httpRequest) {
		return HttpException.ifThrows(() -> httpClient.send(httpRequest, getResponseBodyHandler(apiRequest)));
	}

	/**
	 * Builds the {@link HttpRequest} based on the given {@link ApiRequest} and handles any exception that may occur during
	 * the building process by throwing an {@link HttpException} with a {@link HttpStatus#BAD_REQUEST} status code.
	 * <p>
	 * Note: {@link HttpMethod#CONNECT} is explicitly not supported by the Java net HTTP client so we won't support it
	 * either for this exchange client.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest API request
	 * @return HTTP request object
	 */
	protected <T> HttpRequest buildRequest(final ApiRequest<T> apiRequest) {
		return HttpException.ifThrows(() -> buildHttpRequest(apiRequest), HttpStatus.BAD_REQUEST);
	}

	/**
	 * Builds the {@link HttpRequest} based on the given {@link ApiRequest}.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest API request
	 * @return HTTP request object
	 */
	protected <T> HttpRequest buildHttpRequest(final ApiRequest<T> apiRequest) {
		HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
				.uri(apiRequest.getUri());
		addHeaders(httpRequestBuilder, apiRequest.getHeaders());

		HttpMethod httpMethod = apiRequest.getMethod();
		switch (httpMethod) {
			case GET -> httpRequestBuilder.GET();
			case PUT -> httpRequestBuilder.PUT(toBodyPublisher(apiRequest));
			case POST -> httpRequestBuilder.POST(toBodyPublisher(apiRequest));
			case DELETE -> httpRequestBuilder.DELETE();
			case HEAD -> httpRequestBuilder.HEAD();
			case PATCH -> httpRequestBuilder.method(httpMethod.value(), toBodyPublisher(apiRequest));
			case OPTIONS, TRACE -> httpRequestBuilder.method(httpMethod.value(), BodyPublishers.noBody());
			default -> throw new UnsupportedOperationException("HTTP method " + httpMethod + " is not supported!");
		}
		Nullables.apply(getUsableTimeout(getClientProperties().getTimeout(), Timeout::getRequest), httpRequestBuilder::timeout);
		return httpRequestBuilder.build();
	}

	/**
	 * Builds the {@link ApiResponse} based on the {@link HttpResponse} object.
	 * <p>
	 * TODO: implement response handling for non 2xx responses and other than JSON media type
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 * @param <R> HTTP response body type
	 *
	 * @param apiRequest API request object
	 * @param httpResponse HTTP response object
	 * @return API response object
	 */
	protected <T, U, R> ApiResponse<U> buildResponse(final ApiRequest<T> apiRequest, final HttpResponse<R> httpResponse) {
		HttpStatus httpStatus = HttpStatus.fromCode(httpResponse.statusCode());
		Map<String, List<String>> headers = Nullables.apply(httpResponse.headers(), HttpHeaders::map);

		List<String> encodings = getHeaderValues(HttpHeader.CONTENT_ENCODING, headers);
		R responseBody = ContentEncoding.decodeBody(httpResponse.body(), ContentEncoding.parseAll(encodings));

		List<String> contentTypes = getHeaderValues(HttpHeader.CONTENT_TYPE, headers);
		HttpContentType contentType = HttpContentType.parse(contentTypes);

		if (httpStatus.isError()) {
			throw new HttpException(httpStatus, StringHttpContentConverter.from(responseBody, contentType));
		}
		U body = convertBody(apiRequest, contentType, headers, responseBody);

		return ApiResponse.create(body)
				.status(httpStatus)
				.headers(headers)
				.request(apiRequest)
				.exchangeClient(this)
				.build();
	}

	/**
	 * Creates a {@link BodyPublisher} from the given API request. It checks for the body common types to create the
	 * appropriate publisher.
	 *
	 * @param <T> body type
	 * @param apiRequest the API request object
	 * @return a body publisher needed in building the HTTP request
	 */
	public static <T> BodyPublisher toBodyPublisher(final ApiRequest<T> apiRequest) {
		T body = apiRequest.getBody();
		return toBodyPublisher(apiRequest, body);
	}

	/**
	 * Creates a {@link BodyPublisher} from the given API request and body. It checks for the body common types to create
	 * the appropriate publisher. This method only uses the body parameter to create the publisher and not the API request
	 * body because in some cases the body can be a supplier that needs to be resolved first to get the actual body object
	 * and then create the publisher from it. The API request is only used in this method to get the {@link Charset} for
	 * string bodies and to check if the body should be converted to JSON or not.
	 *
	 * @param <T> body type
	 *
	 * @param apiRequest the API request object
	 * @param body the body to create the publisher from
	 * @return a body publisher needed in building the HTTP request
	 */
	private static <T> BodyPublisher toBodyPublisher(final ApiRequest<T> apiRequest, final T body) {
		if (body == null) {
			return BodyPublishers.noBody();
		}
		Charset charset = Nullables.nonNullOrDefault(apiRequest.getCharset(), Strings.DEFAULT_CHARSET);
		return switch (body) {
			case String str -> BodyPublishers.ofString(str, charset);
			case byte[] bytes -> BodyPublishers.ofByteArray(bytes);
			case InputStream is -> BodyPublishers.ofInputStream(() -> is);
			case InputStreamSupplier iss -> BodyPublishers.ofInputStream(iss);
			case Supplier<?> supplier -> toBodyPublisher(apiRequest, JavaObjects.cast(supplier.get()));
			case Path path -> HttpException.ifThrows(() -> BodyPublishers.ofFile(path), HttpStatus.BAD_REQUEST);
			case Object obj when isJson(apiRequest) -> BodyPublishers.ofString(JsonBuilder.toJson(obj), charset);
			default -> BodyPublishers.ofString(Strings.safeToString(body), charset);
		};
	}

	/**
	 * Returns the body handler based on the request.
	 *
	 * @param <T> request body type
	 * @param <U> body handler type
	 *
	 * @param apiRequest the API request object
	 * @return the body handler based on the request
	 */
	public static <T, U> BodyHandler<U> getResponseBodyHandler(final ApiRequest<T> apiRequest) {
		BodyHandler<?> bodyHandler;
		if (apiRequest.isStream()) {
			bodyHandler = BodyHandlers.ofInputStream();
		} else {
			bodyHandler = BodyHandlers.ofByteArray();
		}
		return JavaObjects.cast(bodyHandler);
	}

	/**
	 * Adds the given headers to the {@link HttpRequest.Builder}.
	 *
	 * @param httpRequestBuilder request builder to add the headers to
	 * @param headers map of headers to add to the request builder
	 */
	public static void addHeaders(final HttpRequest.Builder httpRequestBuilder, final Map<String, List<String>> headers) {
		Maps.safe(headers)
				.forEach((headerName, headerValues) -> Lists.safe(headerValues)
						.forEach(headerValue -> httpRequestBuilder.header(headerName, headerValue)));
	}

	/**
	 * Returns the usable timeout value based on the given timeout and timeout extractor. If the timeout value is equal to
	 * {@link ClientProperties.Timeout#INFINITE}, then this method will return null to indicate that no timeout should be
	 * applied.
	 * <p>
	 * This method is only useful for the Java net HTTP client because whenever a zero (infinite) timeout is set it throws
	 * an exception instead of just treating it as infinite.
	 *
	 * @param timeout timeout object containing the timeout value
	 * @param timeoutExtractor function to extract the timeout value from the timeout object
	 * @return the usable timeout value or null if no timeout should be applied
	 */
	protected static Duration getUsableTimeout(final Timeout timeout, final Function<Timeout, Duration> timeoutExtractor) {
		Duration timeoutValue = timeoutExtractor.apply(timeout);
		if (null == timeoutValue) {
			return null;
		}
		if (Objects.equals(timeoutValue, ClientProperties.Timeout.INFINITE)) {
			return null;
		}
		return timeoutValue;
	}
}
