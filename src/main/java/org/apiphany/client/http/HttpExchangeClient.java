package org.apiphany.client.http;

import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpProperties;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ThrowingSupplier;

/**
 * Basic HTTP exchange client. The default HTTP version used is HTTP/1.1.
 *
 * @author Radu Sebastian LAZIN
 */
public class HttpExchangeClient extends AbstractHttpExchangeClient {

	/**
	 * Underlying Java net HTTP client.
	 */
	private final HttpClient httpClient;

	/**
	 * Default constructor will initialize the client with default client properties. See the {@link ClientProperties} class
	 * to see the defaults.
	 */
	public HttpExchangeClient() {
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
	public HttpExchangeClient(final ClientProperties clientProperties, final String prefix, final String clientName) {
		this(clientProperties.getClientProperties(prefix, clientName));
	}

	/**
	 * Initialize the client with the given client properties.
	 *
	 * @param clientProperties client properties
	 */
	public HttpExchangeClient(final ClientProperties clientProperties) {
		super(clientProperties);
		this.httpClient = createClient(this::customize);
		addDefaultHeaderValues(getHeaderValuesChain());
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
		HttpProperties httpProperties = getClientProperties().getCustomProperties(HttpProperties.class);
		HttpClient.Version version = Nullables.notNull(httpProperties)
				.thenYield(props -> props.getRequest().getHttpVersion())
				.orElse(() -> HttpProperties.Request.DEFAULT_HTTP_VERSION);
		httpClientBuilder.version(version);
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

		HttpMethod httpMethpd = apiRequest.getMethod();
		String stringBody = Strings.safeToString(apiRequest.getBody());
		switch (httpMethpd) {
			case GET -> httpRequestBuilder.GET();
			case PUT -> httpRequestBuilder.PUT(BodyPublishers.ofString(stringBody));
			case POST -> httpRequestBuilder.POST(BodyPublishers.ofString(stringBody));
			case DELETE -> httpRequestBuilder.DELETE();
			case HEAD -> httpRequestBuilder.HEAD();
			case PATCH -> httpRequestBuilder.method(httpMethpd.value(), BodyPublishers.ofString(stringBody));
			case OPTIONS -> httpRequestBuilder.method(httpMethpd.value(), BodyPublishers.noBody());
			default -> throw new UnsupportedOperationException("HTTP method " + httpMethpd + " is not supported!");
		}
		addHeaders(httpRequestBuilder, apiRequest.getHeaders());

		return httpRequestBuilder.build();
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
	protected <T, U> ApiResponse<U> buildResponse(final ApiRequest<T> apiRequest, final HttpResponse<String> httpResponse) {
		HttpStatus httpStatus = HttpStatus.from(httpResponse.statusCode());
		if (httpStatus.isError()) {
			throw new HttpException(httpStatus, httpResponse.body());
		}
		Map<String, List<String>> headers = Nullables.apply(httpResponse.headers(), HttpHeaders::map);
		U body = convertBody(apiRequest, httpResponse.headers(), httpResponse.body());

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
	protected void addHeaders(final HttpRequest.Builder httpRequestBuilder, final Map<String, List<String>> headers) {
		Maps.safe(headers).forEach((k, v) -> v.forEach(h -> httpRequestBuilder.header(k, h)));
	}

}
