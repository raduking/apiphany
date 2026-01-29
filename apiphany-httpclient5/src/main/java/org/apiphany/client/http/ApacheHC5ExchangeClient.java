package org.apiphany.client.http;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.HttpEntities;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Lists;
import org.apiphany.lang.collections.Maps;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ThrowingSupplier;

/**
 * Apache HTTP Client 5 exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApacheHC5ExchangeClient extends AbstractHttpExchangeClient {

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
	public ApacheHC5ExchangeClient() {
		this(new ClientProperties());
	}

	/**
	 * Constructs the exchange client.
	 *
	 * @param clientProperties the client properties
	 */
	public ApacheHC5ExchangeClient(final ClientProperties clientProperties) {
		super(clientProperties);
		this.httpClient = PoolingHttpClients.createClient(clientProperties,
				PoolingHttpClients.noCustomizer(), this::customize, this::customize);
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
		ApacheHC5Properties properties = getClientProperties().getCustomProperties(ApacheHC5Properties.class);
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
	@SuppressWarnings("resource")
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		apiRequest.addHeaders(getTracingHeaders());
		apiRequest.addHeaders(getCommonHeaders());

		HttpUriRequest httpUriRequest = buildRequest(apiRequest);
		HttpClientResponseHandler<ApiResponse<U>> responseHandler = httpResponse -> buildResponse(apiRequest, httpResponse);
		return ThrowingSupplier
				.unchecked(() -> getHttpClient().execute(httpUriRequest, responseHandler))
				.get();
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
		String contentTypeValue = Lists.first(MapHeaderValues.get(HttpHeader.CONTENT_TYPE, apiRequest.getHeaders()));
		ContentType contentType = Nullables.apply(contentTypeValue, ct -> ContentType.parse(ct).withCharset(apiRequest.getCharset()));
		return switch (body) {
			case String str -> HttpEntities.create(str, contentType);
			case byte[] bytes -> HttpEntities.create(bytes, contentType);
			case File file -> HttpEntities.create(file, contentType);
			case Serializable serializable -> HttpEntities.create(serializable, contentType);
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

		Map<String, List<String>> headers = Nullables.apply(response.getHeaders(), ApacheHC5ExchangeClient::toHttpHeadersMap);
		HttpContentType resolvedContentType = HttpContentType.from(httpEntity.getContentType(), httpEntity.getContentEncoding());
		if (httpStatus.isError()) {
			throw new HttpException(httpStatus, StringHttpContentConverter.instance().from(httpEntity, resolvedContentType, String.class));
		}

		byte[] bodyBytes = ThrowingSupplier.unchecked(() -> EntityUtils.toByteArray(httpEntity)).get();
		U body = convertBody(apiRequest, resolvedContentType, headers, bodyBytes);

		return ApiResponse.create(body)
				.status(httpStatus)
				.headers(headers)
				.exchangeClient(this)
				.build();
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
			String headerName = header.getName();
			String headerValue = header.getValue();
			httpHeaders.computeIfAbsent(headerName, k -> new ArrayList<>()).add(headerValue);
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
