package org.apiphany.client.http;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpOptions;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpTrace;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.Headers;
import org.apiphany.http.ApacheHC5Entities;
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
 * Apache HTTP Client 5 exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApacheHC5HttpExchangeClient extends AbstractHttpExchangeClient {

	/**
	 * The Apache HTTP client instance.
	 */
	private final CloseableHttpClient httpClient;

	/**
	 * The polling HTTP client connection manager.
	 */
	private PoolingHttpClientConnectionManager connectionManager;

	/**
	 * The HTTP protocol version.
	 */
	private ProtocolVersion httpVersion;

	/**
	 * Constructs the exchange client.
	 */
	public ApacheHC5HttpExchangeClient() {
		this(ClientProperties.defaults());
	}

	/**
	 * Constructs the exchange client.
	 *
	 * @param clientProperties the client properties
	 */
	public ApacheHC5HttpExchangeClient(final ClientProperties clientProperties) {
		super(clientProperties);
		this.httpClient = ApacheHC5PoolingHttpClients.createClient(clientProperties,
				ApacheHC5PoolingHttpClients.noCustomizer(), this::customize, this::customize);
		this.httpVersion = Nullables.nonNullOrDefault(this.httpVersion, HttpVersion.DEFAULT);
	}

	/**
	 * Customizes the connection manager.
	 *
	 * @param connectionManager pooling HTTP client connection manager
	 */
	private void customize(final PoolingHttpClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	/**
	 * Customizes the HTTP client builder.
	 *
	 * @param httpClientBuilder the HTTP client builder
	 */
	private void customize(final HttpClientBuilder httpClientBuilder) {
		if (!ApacheHC5Properties.Connection.DEFAULT_FOLLOW_REDIRECTS) {
			httpClientBuilder.disableRedirectHandling();
		}
		ApacheHC5Properties properties = getCustomProperties(ApacheHC5Properties.class);
		if (null == properties) {
			return;
		}
		this.httpVersion = properties.getRequest().getHttpProtocolVersion();
	}

	/**
	 * @see #close()
	 */
	@Override
	public void close() throws Exception {
		httpClient.close();
	}

	/**
	 * @see ExchangeClient#exchange(ApiRequest)
	 */
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		apiRequest.addHeaders(getTracingHeaders());
		apiRequest.addHeaders(getCommonHeaders());

		HttpUriRequest httpUriRequest = buildRequest(apiRequest);
		return sendRequest(apiRequest, httpUriRequest);
	}

	/**
	 * Sends the HTTP request and returns the API response.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest API request object
	 * @param httpUriRequest HTTP URI request to send
	 * @return API response object
	 */
	@SuppressWarnings("resource")
	protected <U, T> ApiResponse<U> sendRequest(final ApiRequest<T> apiRequest, final HttpUriRequest httpUriRequest) {
		HttpClientResponseHandler<ApiResponse<U>> responseHandler = httpResponse -> buildResponse(apiRequest, httpResponse);
		return HttpException.ifThrows(() -> getHttpClient().execute(httpUriRequest, responseHandler));
	}

	/**
	 * Builds the HTTP URI request object.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest the API request object
	 * @return a HTTP URI request
	 */
	protected <T> HttpUriRequest buildRequest(final ApiRequest<T> apiRequest) {
		HttpUriRequest httpUriRequest = toHttpUriRequest(apiRequest.getUri(), apiRequest.<HttpMethod>getMethod());
		addHeaders(httpUriRequest, apiRequest.getHeaders());
		httpUriRequest.setVersion(httpVersion);

		if (apiRequest.hasBody()) {
			// This entity doesn't need to be closed
			@SuppressWarnings("resource")
			HttpEntity httpEntity = createHttpEntity(apiRequest);
			httpUriRequest.setEntity(httpEntity);
		}
		return httpUriRequest;
	}

	/**
	 * Creates an appropriate {@link HttpEntity} based on the request body type and headers.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest API request object
	 * @return HTTP entity object
	 */
	protected <T> HttpEntity createHttpEntity(final ApiRequest<T> apiRequest) {
		T body = apiRequest.getBody();
		List<String> contentTypeValues = getHeaderValues(HttpHeader.CONTENT_TYPE, apiRequest.getHeaders());
		String contentTypeValue = Lists.first(contentTypeValues);
		ContentType contentType = Nullables.apply(contentTypeValue, ct -> ContentType.parse(ct).withCharset(apiRequest.getCharset()));
		return createHttpEntity(apiRequest, body, contentType);
	}

	/**
	 * Creates an appropriate {@link HttpEntity} based on the request body type and content type.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest API request object
	 * @param body request body
	 * @param contentType content type of the request body
	 * @return HTTP entity object
	 */
	private static <T> HttpEntity createHttpEntity(final ApiRequest<T> apiRequest, final T body, final ContentType contentType) {
		return switch (body) {
			case String str -> HttpEntities.create(str, contentType);
			case byte[] bytes -> HttpEntities.create(bytes, contentType);
			case InputStream is -> ApacheHC5Entities.create(is, contentType);
			case InputStreamSupplier iss -> ApacheHC5Entities.create(iss.get(), contentType);
			case Supplier<?> supplier -> createHttpEntity(apiRequest, JavaObjects.cast(supplier.get()), contentType);
			case File file -> HttpEntities.create(file, contentType);
			case Serializable serializable -> HttpEntities.create(serializable, contentType);
			case Object obj when isJson(apiRequest) -> HttpEntities.create(JsonBuilder.toJson(body), contentType);
			default -> HttpEntities.create(Strings.safeToString(body), contentType);
		};
	}

	/**
	 * Builds the API response object.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest API request object
	 * @param response Apache HTTP response
	 * @return API response object
	 */
	@SuppressWarnings("resource")
	protected <T, U> ApiResponse<U> buildResponse(final ApiRequest<T> apiRequest, final ClassicHttpResponse response) {
		HttpEntity httpEntity = response.getEntity();
		HttpStatus httpStatus = HttpStatus.fromCode(response.getCode());
		Map<String, List<String>> headers = Nullables.whenNotNull(response.getHeaders(), ApacheHC5HttpExchangeClient::toHttpHeadersMap);

		List<String> encodings = getHeaderValues(HttpHeader.CONTENT_ENCODING, headers);
		U responseBody = ContentEncoding.decodeBody(getResponseBody(apiRequest, httpEntity), ContentEncoding.parseAll(encodings));

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
	 * Returns the response body converted to the target type. If the request is a stream, the response body is returned as
	 * an input stream, otherwise it is returned as a byte array.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest API request object
	 * @param httpEntity HTTP entity containing the response body
	 * @return the response body converted to the target type
	 */
	@SuppressWarnings("resource")
	protected <T, U> U getResponseBody(final ApiRequest<T> apiRequest, final HttpEntity httpEntity) {
		Object body;
		if (apiRequest.isStream()) {
			body = ApacheHC5Entities.toInputStream(httpEntity);
		} else {
			body = ApacheHC5Entities.toByteArray(httpEntity);
		}
		return JavaObjects.cast(body);
	}

	/**
	 * Adds the given headers to the {@link HttpUriRequest}.
	 *
	 * @param httpUriRequest request to add the headers to
	 * @param headers map of headers to add to the request
	 */
	public static void addHeaders(final HttpUriRequest httpUriRequest, final Map<String, List<String>> headers) {
		Maps.safe(headers)
				.forEach((headerName, headerValues) -> Lists.safe(headerValues)
						.forEach(headerValue -> httpUriRequest.addHeader(headerName, headerValue)));
	}

	/**
	 * Transforms a {@link URI} and a {@link HttpMethod} to a {@link HttpUriRequest}.
	 *
	 * @param uri URI
	 * @param httpMethod HTTP method
	 * @return a new HTTP URI request
	 */
	public static HttpUriRequest toHttpUriRequest(final URI uri, final HttpMethod httpMethod) {
		return switch (httpMethod) {
			case GET -> new HttpGet(uri);
			case POST -> new HttpPost(uri);
			case PUT -> new HttpPut(uri);
			case DELETE -> new HttpDelete(uri);
			case PATCH -> new HttpPatch(uri);
			case HEAD -> new HttpHead(uri);
			case OPTIONS -> new HttpOptions(uri);
			case TRACE -> new HttpTrace(uri);
			default -> throw new UnsupportedOperationException("HTTP method " + httpMethod + " is not supported!");
		};
	}

	/**
	 * Transforms a {@link URI} and a {@link HttpMethod} to a {@link HttpUriRequest}.
	 *
	 * @param uri URI
	 * @param httpMethod HTTP method
	 * @return a new HTTP URI request
	 */
	public static HttpUriRequest toHttpUriRequest(final URI uri, final String httpMethod) {
		return toHttpUriRequest(uri, HttpMethod.fromString(httpMethod));
	}

	/**
	 * Transforms an array of {@link Header}s to a map of headers.
	 *
	 * @param headers source headers
	 * @return HTTP headers
	 */
	public static Map<String, List<String>> toHttpHeadersMap(final Header[] headers) {
		var httpHeaders = new HashMap<String, List<String>>();
		for (Header header : headers) {
			Headers.addTo(httpHeaders, header.getName(), header.getValue());
		}
		return httpHeaders;
	}

	/**
	 * Returns the underlying Apache HTTP Client 5.
	 *
	 * @return the underlying Apache HTTP Client 5
	 */
	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * Returns the connection manager.
	 *
	 * @return the connection manager
	 */
	public PoolingHttpClientConnectionManager getConnectionManager() {
		return connectionManager;
	}

}
