package org.apiphany;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpClientFluentAdapter;
import org.apiphany.header.Headers;
import org.apiphany.lang.Strings;
import org.apiphany.lang.collections.Maps;
import org.apiphany.lang.retry.Retry;
import org.apiphany.meters.BasicMeters;
import org.apiphany.security.AuthenticationType;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.reflection.GenericClass;

import io.micrometer.core.instrument.Tags;

/**
 * Adapter for fluent style syntax. This class functions similar to a builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientFluentAdapter extends ApiRequest<Object> {

	/**
	 * The underlying API client.
	 */
	private final ApiClient apiClient;

	/**
	 * The underlying Exchange client.
	 */
	private ExchangeClient exchangeClient;

	/**
	 * Constructs the object with the given API client.
	 *
	 * @param apiClient the underlying API client
	 */
	protected ApiClientFluentAdapter(final ApiClient apiClient) {
		this.apiClient = Objects.requireNonNull(apiClient);
	}

	/**
	 * Factory method to construct the object with a given API client.
	 *
	 * @param apiClient the underlying API client
	 * @return a new API client fluent adapter object
	 */
	public static ApiClientFluentAdapter of(final ApiClient apiClient) {
		return new ApiClientFluentAdapter(apiClient);
	}

	/**
	 * Sets the authentication type and also internally the corresponding exchange client if needed.
	 *
	 * @param authenticationType the authentication type to set
	 * @return this
	 */
	@SuppressWarnings("resource")
	public ApiClientFluentAdapter authenticationType(final AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
		if (null == exchangeClient || authenticationType != exchangeClient.getAuthenticationType()) {
			return exchangeClient(apiClient.getExchangeClient(authenticationType));
		}
		return this;
	}

	/**
	 * Sets the exchange client and the authentication type from the given exchange client.
	 *
	 * @param exchangeClient exchange client to set
	 * @return this
	 */
	protected ApiClientFluentAdapter exchangeClient(final ExchangeClient exchangeClient) {
		this.exchangeClient = exchangeClient;
		return authenticationType(exchangeClient.getAuthenticationType());
	}

	/**
	 * Retrieves the API response.
	 *
	 * @param <T> response type
	 * @return an API response object
	 */
	public <T> ApiResponse<T> retrieve() {
		if (isUrlEncoded()) {
			this.params = RequestParameters.encode(this.params, getCharset());
		}
		return JavaObjects.cast(apiClient.exchange(this));
	}

	/**
	 * Retrieves the API response.
	 *
	 * @param <T> response type
	 *
	 * @param responseType the response type class
	 * @return an API response object
	 */
	public <T> ApiResponse<T> retrieve(final Class<T> responseType) {
		return responseType(responseType).retrieve();
	}

	/**
	 * Retrieves the API response.
	 *
	 * @param <T> response type
	 *
	 * @param responseType the response type generic class
	 * @return an API response object
	 */
	public <T> ApiResponse<T> retrieve(final GenericClass<T> responseType) {
		return responseType(responseType).retrieve();
	}

	/**
	 * Downloads content.
	 *
	 * @param <T> response type
	 * @return an API response object
	 */
	public <T> ApiResponse<T> download() {
		return stream().retrieve();
	}

	/**
	 * Sets the request method.
	 *
	 * @param method the method to set
	 * @return this
	 */
	public ApiClientFluentAdapter method(final RequestMethod method) {
		this.method = method;
		return this;
	}

	/**
	 * Sets the URL as string.
	 *
	 * @param url the URL to set
	 * @return this
	 */
	public ApiClientFluentAdapter url(final String url) {
		this.url = url;
		return this;
	}

	/**
	 * Sets the URL by concatenating the given URL with the given path segments.
	 *
	 * @param url the URL to set
	 * @param pathSegments the path segments to append to the URL
	 * @return this
	 */
	public ApiClientFluentAdapter url(final String url, final String... pathSegments) {
		if (Strings.isEmpty(url)) {
			throw new IllegalArgumentException("url cannot be null or empty");
		}
		StringBuilder sb = new StringBuilder(url);
		for (String pathSegment : pathSegments) {
			if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != '/') {
				sb.append('/');
			}
			String segment = isUrlEncoded() ? URLEncoder.encode(pathSegment, charset) : pathSegment;
			sb.append(segment);
		}
		return url(sb.toString());
	}

	/**
	 * Sets the URL from the given URI.
	 *
	 * @param uri the URI
	 * @return this
	 */
	public ApiClientFluentAdapter uri(final URI uri) {
		return url(uri.toString());
	}

	/**
	 * Sets the URL by concatenating the given URI with the given path segments.
	 *
	 * @param uri the URI
	 * @param pathSegments the path segments to append to the URL
	 * @return this
	 */
	public ApiClientFluentAdapter uri(final URI uri, final String... pathSegments) {
		return url(uri.toString(), pathSegments);
	}

	/**
	 * Sets the URL by concatenating the {@link ApiClient#getBaseUrl()} with the given path segments.
	 *
	 * @param pathSegments the path segments to append to the URL
	 * @return this
	 */
	public ApiClientFluentAdapter path(final String... pathSegments) {
		String baseUrl = apiClient.getBaseUrl();
		if (ApiClient.EMPTY_BASE_URL.equals(baseUrl)) {
			baseUrl = url;
		}
		return url(baseUrl, pathSegments);
	}

	/**
	 * Sets the URL by concatenating the {@link ApiClient#getBaseUrl()} with the given path segments encoding them.
	 *
	 * @param pathSegments the path segments to append to the URL that will be encoded
	 * @return this
	 */
	public ApiClientFluentAdapter pathEncoded(final String... pathSegments) {
		return urlEncoded().path(pathSegments);
	}

	/**
	 * Signals the need to encode the path segments or request parameters.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter urlEncoded() {
		return urlEncode(true);
	}

	/**
	 * Signals the need to encode or not the path segments or request parameters depending on the given flag.
	 *
	 * @param encode flag to encode or not the path segments or request parameters
	 * @return this
	 */
	public ApiClientFluentAdapter urlEncode(final boolean encode) {
		this.urlEncoded = encode;
		return this;
	}

	/**
	 * Sets the request body.
	 *
	 * @param <U> body type
	 *
	 * @param body the body to set
	 * @return this
	 */
	public <U> ApiClientFluentAdapter body(final U body) {
		this.body = body;
		return this;
	}

	/**
	 * Alias for {@link #body(Object)}.
	 *
	 * @param <U> payload type
	 *
	 * @param payload the payload to set
	 * @return this
	 */
	public <U> ApiClientFluentAdapter payload(final U payload) {
		return body(payload);
	}

	/**
	 * Sets the response type as a class, this is exclusive with {@link #responseType(GenericClass)}.
	 *
	 * @param <T> response body type
	 * @param responseType response type
	 * @return this
	 */
	public <T> ApiClientFluentAdapter responseType(final Class<T> responseType) {
		Nullables.requireNull(genericResponseType, "Class response type already set");
		this.classResponseType = responseType;
		return this;
	}

	/**
	 * Sets the response type as a generic class, this is exclusive with {@link #responseType(Class)}.
	 *
	 * @param <T> response body type
	 * @param responseType response type
	 * @return this
	 */
	public <T> ApiClientFluentAdapter responseType(final GenericClass<T> responseType) {
		Nullables.requireNull(classResponseType, "Generic class response type already set");
		this.genericResponseType = responseType;
		return this;
	}

	/**
	 * Sets the request parameters. If the input request parameters is {@code null} then the request parameters will be set
	 * to an empty map.
	 *
	 * @param requestParams request parameters
	 * @return this
	 */
	public ApiClientFluentAdapter params(final Map<String, String> requestParams) {
		Objects.requireNonNull(getUrl(), "Request parameters must be set after URL/URI");
		this.params = Maps.safe(requestParams);
		return this;
	}

	/**
	 * Sets the request parameters.
	 *
	 * @param paramFunctions request parameter functions
	 * @return this
	 */
	public ApiClientFluentAdapter params(final ParameterFunction... paramFunctions) {
		return params(RequestParameters.of(paramFunctions));
	}

	/**
	 * Sets the headers.
	 *
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param headers headers map
	 * @return this
	 */
	public <N, H> ApiClientFluentAdapter headers(final Map<N, H> headers) {
		Headers.addTo(this.headers, headers);
		return this;
	}

	/**
	 * Sets the headers if the condition is true.
	 *
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param condition condition on when to add the supplied headers
	 * @param headersSupplier headers map supplier
	 * @return this
	 */
	public <N, H> ApiClientFluentAdapter headersWhen(final boolean condition, final Supplier<Map<N, H>> headersSupplier) {
		return condition ? headers(headersSupplier.get()) : this;
	}

	/**
	 * Adds a header.
	 *
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param headerName header name
	 * @param headerValue header value
	 * @return this
	 */
	public <N, H> ApiClientFluentAdapter header(final N headerName, final H headerValue) {
		Headers.addTo(headers, headerName, headerValue);
		return this;
	}

	/**
	 * Adds a header when the given condition is true.
	 *
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param condition condition when to add the headers
	 * @param headerName header name
	 * @param headerValue header value
	 * @return this
	 */
	public <N, H> ApiClientFluentAdapter headerWhen(final boolean condition, final N headerName, final H headerValue) {
		return headerWhen(condition, headerName, () -> headerValue);
	}

	/**
	 * Adds a header when the given condition is true.
	 *
	 * @param <N> header name type
	 * @param <H> header value type
	 *
	 * @param condition condition when to add the headers
	 * @param headerName header name
	 * @param headerValueSupplier header value supplier
	 * @return this
	 */
	public <N, H> ApiClientFluentAdapter headerWhen(final boolean condition, final N headerName, final Supplier<H> headerValueSupplier) {
		return condition ? header(headerName, headerValueSupplier.get()) : this;
	}

	/**
	 * Sets the character set.
	 *
	 * @param charset character set
	 * @return this
	 */
	public ApiClientFluentAdapter charset(final Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * Sets the stream flag.
	 *
	 * @param stream value to set
	 * @return this
	 */
	public ApiClientFluentAdapter stream(final boolean stream) {
		this.stream = stream;
		return this;
	}

	/**
	 * Sets the stream flag to true.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter stream() {
		return stream(true);
	}

	/**
	 * Adds meters with the given prefix.
	 *
	 * @param prefix meters prefix
	 * @return this
	 */
	public ApiClientFluentAdapter meters(final String prefix) {
		return meters(BasicMeters.of(apiClient.getMeterFactory(), prefix));
	}

	/**
	 * Adds meters with the given prefix and tags.
	 *
	 * @param prefix meters prefix
	 * @param tags meters tags
	 * @return this
	 */
	public ApiClientFluentAdapter meters(final String prefix, final Tags tags) {
		return meters(BasicMeters.of(apiClient.getMeterFactory(), prefix, tags));
	}

	/**
	 * Sets meters on the method that called one of the {@code retrieve} methods.
	 *
	 * @param prefix meters prefix
	 * @return this
	 */
	public ApiClientFluentAdapter metersOnMethod(final String prefix) {
		return meters(BasicMeters.onCallerMethod(apiClient.getMeterFactory(), prefix));
	}

	/**
	 * Sets meters on the method that called one of the {@code retrieve} methods with the given prefix or tags.
	 *
	 * @param prefix meters prefix
	 * @param tags meters tags
	 * @return this
	 */
	public ApiClientFluentAdapter metersOnMethod(final String prefix, final Tags tags) {
		return meters(BasicMeters.onCallerMethod(apiClient.getMeterFactory(), prefix, tags));
	}

	/**
	 * Sets the basic meters.
	 *
	 * @param meters basic meters
	 * @return this
	 */
	public ApiClientFluentAdapter meters(final BasicMeters meters) {
		this.meters = meters;
		return this;
	}

	/**
	 * Sets the retry.
	 *
	 * @param retry retry
	 * @return this
	 */
	public ApiClientFluentAdapter retry(final Retry retry) {
		this.retry = retry;
		return this;
	}

	/**
	 * Sets the default retry.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter defaultRetry() {
		return retry(Retry.defaultRetry());
	}

	/**
	 * Sets all the information from the given API request except the response type and authentication type.
	 *
	 * @param <T> body type
	 * @param apiRequest API request object
	 * @return this
	 */
	public <T> ApiClientFluentAdapter apiRequest(final ApiRequest<T> apiRequest) {
		return url(apiRequest.getUrl())
				.method(apiRequest.getMethod())
				.params(apiRequest.getParams())
				.headers(apiRequest.getHeaders())
				.body(apiRequest.getBody())
				.charset(apiRequest.getCharset())
				.urlEncode(apiRequest.isUrlEncoded())
				.stream(apiRequest.isStream())
				.meters(apiRequest.getMeters())
				.retry(apiRequest.getRetry());
	}

	/**
	 * @see #getHeadersAsString()
	 */
	@SuppressWarnings("resource")
	@Override
	public String getHeadersAsString() {
		return getExchangeClient(ExchangeClient.class).getHeadersAsString(this);
	}

	/**
	 * Returns the required exchange class.
	 *
	 * @param <T> exchange client class type
	 *
	 * @param exchangeClientClass exchange client class
	 * @return the required exchange class
	 */
	public <T extends ExchangeClient> T getExchangeClient(final Class<T> exchangeClientClass) {
		if (!exchangeClientClass.isAssignableFrom(exchangeClient.getClass())) {
			throw new IllegalArgumentException("The underlying exchange client cannot be cast to: " + exchangeClientClass);
		}
		return JavaObjects.cast(exchangeClient);
	}

	/**
	 * Exposes the HTTP specific methods.
	 *
	 * @return a new HTTP Client fluent adapter
	 */
	public HttpClientFluentAdapter http() {
		return HttpClientFluentAdapter.of(this);
	}

}
