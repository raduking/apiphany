package org.apiphany.client.http;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.ContentType;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ThrowingSupplier;

/**
 * Java HTTP exchange client based on the default {@link HttpClient}. The default HTTP version used is HTTP/1.1.
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
		this(new ClientProperties());
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
	 * Initialize the client with the given client properties.
	 *
	 * @param clientProperties client properties
	 */
	public JavaNetHttpExchangeClient(final ClientProperties clientProperties) {
		super(clientProperties);
		this.httpClient = createClient(this::customize);
	}

	/**
	 * @see #close()
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
	 */
	private void customize(final HttpClient.Builder httpClientBuilder) {
		JavaNetHttpProperties httpProperties = getClientProperties().getCustomProperties(JavaNetHttpProperties.class);
		HttpClient.Version version = Nullables.notNull(httpProperties)
				.thenYield(props -> props.getRequest().getHttpVersion())
				.orElse(() -> JavaNetHttpProperties.Request.DEFAULT_HTTP_VERSION);
		httpClientBuilder.version(version);
		Nullables.notNull(getSslContext()).then(httpClientBuilder::sslContext);
	}

	/**
	 * @see ExchangeClient#exchange(ApiRequest)
	 */
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		HttpRequest httpRequest = buildRequest(apiRequest);
		HttpResponse<String> httpResponse = ThrowingSupplier
				.unchecked(() -> httpClient.send(httpRequest, BodyHandlers.ofString()))
				.get();
		return buildResponse(apiRequest, httpResponse);
	}

	/**
	 * Builds the {@link HttpRequest} based on the given {@link ApiRequest}.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest API request
	 * @return HTTP request object
	 */
	protected <T> HttpRequest buildRequest(final ApiRequest<T> apiRequest) {
		HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
				.uri(apiRequest.getUri());
		addTracingHeaders(apiRequest.getHeaders());
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
			default -> throw new HttpException(HttpStatus.BAD_REQUEST, "HTTP method " + httpMethod + " is not supported!");
		}
		return httpRequestBuilder.build();
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
		if (body == null) {
			return BodyPublishers.noBody();
		}
		Charset charset = Nullables.nonNullOrDefault(apiRequest.getCharset(), Strings.DEFAULT_CHARSET);
		return switch (body) {
			case String str -> BodyPublishers.ofString(str, charset);
			case byte[] bytes -> BodyPublishers.ofByteArray(bytes);
			case InputStream is -> BodyPublishers.ofInputStream(() -> is);
			case Supplier<?> supplier when supplier.get() instanceof InputStream is -> BodyPublishers.ofInputStream(() -> is);
			case Path path -> HttpException.ifThrows(() -> BodyPublishers.ofFile(path), HttpStatus.BAD_REQUEST);
			case Object obj when apiRequest.containsHeader(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON) -> BodyPublishers
					.ofString(JsonBuilder.toJson(obj));
			default -> BodyPublishers.ofString(Strings.safeToString(body), charset);
		};
	}

	/**
	 * Builds the {@link ApiResponse} based on the {@link HttpResponse} object.
	 * <p>
	 * TODO: implement response handling for non 2xx responses and other than JSON media type
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest API request object
	 * @param httpResponse HTTP response object
	 * @return API response object
	 */
	protected <T, U, R> ApiResponse<U> buildResponse(final ApiRequest<T> apiRequest, final HttpResponse<R> httpResponse) {
		HttpStatus httpStatus = HttpStatus.from(httpResponse.statusCode());
		if (httpStatus.isError()) {
			throw new HttpException(httpStatus, Strings.safeToString(httpResponse.body()));
		}
		Map<String, List<String>> headers = Nullables.apply(httpResponse.headers(), HttpHeaders::map);
		U body = convertBody(apiRequest, headers, httpResponse.body());

		return ApiResponse.create(body)
				.status(httpStatus)
				.headers(headers)
				.exchangeClient(this)
				.build();
	}

	/**
	 * Adds the given headers to the {@link HttpRequest.Builder}.
	 *
	 * @param httpRequestBuilder request builder to add the headers to
	 * @param headers map of headers to add to the request builder
	 */
	public static void addHeaders(final HttpRequest.Builder httpRequestBuilder, final Map<String, List<String>> headers) {
		Maps.safe(headers).forEach((k, v) -> v.forEach(h -> httpRequestBuilder.header(k, h)));
	}

}
