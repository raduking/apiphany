package org.apiphany;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apiphany.RequestParameters.ParameterFunction;
import org.apiphany.auth.AuthenticationType;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.Headers;
import org.apiphany.lang.retry.Retry;
import org.apiphany.meters.BasicMeters;
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
		this.apiClient = apiClient;
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
	 * Sets the authentication type and also internally the corresponding exchange client.
	 *
	 * @param authenticationType the authentication type to set
	 * @return this
	 */
	public ApiClientFluentAdapter authenticationType(final AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
		this.exchangeClient = apiClient.getExchangeClient(authenticationType);
		return this;
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
	 * Sets the HTTP method.
	 *
	 * @param requestMethod the method to set
	 * @return this
	 */
	public ApiClientFluentAdapter requestMethod(final RequestMethod requestMethod) {
		this.method = requestMethod;
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
		StringBuilder sb = new StringBuilder(url);
		for (String pathSegment : pathSegments) {
			if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
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
		return url(apiClient.getBaseUrl(), pathSegments);
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
	 * Sets the request parameters.
	 *
	 * @param requestParams request parameters
	 * @return this
	 */
	public ApiClientFluentAdapter params(final Map<String, String> requestParams) {
		Objects.requireNonNull(requestParams, "Request parameters cannot be null");
		Objects.requireNonNull(getUrl(), "Request parameters must be set after URL/URI");
		this.params = requestParams;
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
	 * @param headers headers map
	 * @return this
	 */
	public <H> ApiClientFluentAdapter headers(final Map<String, H> headers) {
		Headers.addTo(this.headers, headers);
		return this;
	}

	/**
	 * Sets the headers if the condition is true.
	 *
	 * @param condition condition on when to add the supplied headers
	 * @param headersSupplier headers map supplier
	 * @return this
	 */
	public <H> ApiClientFluentAdapter headers(final boolean condition, final Supplier<Map<String, H>> headersSupplier) {
		return condition ? headers(headersSupplier.get()) : this;
	}

	/**
	 * Adds a header.
	 *
	 * @param headerName header name
	 * @param headerValue header value
	 * @return this
	 */
	public <H> ApiClientFluentAdapter header(final String headerName, final H headerValue) {
		Headers.addTo(headers, headerName, headerValue);
		return this;
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
	 * Sets the stream flag to true.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter stream() {
		this.stream = true;
		return this;
	}

	/**
	 * Adds meters with the given prefix.
	 *
	 * @param prefix meters prefix
	 * @return this
	 */
	public ApiClientFluentAdapter meters(final String prefix) {
		return meters(BasicMeters.of(prefix));
	}

	/**
	 * Adds meters with the given prefix and tags.
	 *
	 * @param prefix meters prefix
	 * @param tags meters tags
	 * @return this
	 */
	public ApiClientFluentAdapter meters(final String prefix, final Tags tags) {
		return meters(BasicMeters.of(prefix, tags));
	}

	/**
	 * Sets meters on the method that called one of the {@code retrieve} methods.
	 *
	 * @param prefix meters prefix
	 * @return this
	 */
	public ApiClientFluentAdapter metersOnMethod(final String prefix) {
		return meters(BasicMeters.onCallerMethod(prefix));
	}

	/**
	 * Sets meters on the method that called one of the {@code retrieve} methods with the given prefix or tags.
	 *
	 * @param prefix meters prefix
	 * @param tags meters tags
	 * @return this
	 */
	public ApiClientFluentAdapter metersOnMethod(final String prefix, final Tags tags) {
		return meters(BasicMeters.onCallerMethod(prefix, tags));
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
	 * Sets the HTTP method to GET.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter get() {
		return requestMethod(exchangeClient.get());
	}

	/**
	 * Sets the HTTP method to PUT.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter put() {
		return requestMethod(exchangeClient.put());
	}

	/**
	 * Sets the HTTP method to POST.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter post() {
		return requestMethod(exchangeClient.post());
	}

	/**
	 * Sets the HTTP method to DELETE.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter delete() {
		return requestMethod(exchangeClient.delete());
	}

	/**
	 * Sets the HTTP method to PATCH.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter patch() {
		return requestMethod(exchangeClient.patch());
	}

	/**
	 * Sets the HTTP method to HEAD.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter head() {
		return requestMethod(exchangeClient.head());
	}

	/**
	 * Sets the HTTP method to TRACE.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter trace() {
		return requestMethod(exchangeClient.trace());
	}

	/**
	 * @see #getHeadersAsString()
	 */
	@Override
	public String getHeadersAsString() {
		return exchangeClient.getHeadersAsString(this);
	}

	/**
	 * @see #equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/**
	 * @see #hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
