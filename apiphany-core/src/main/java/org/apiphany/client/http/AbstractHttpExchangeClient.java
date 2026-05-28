package org.apiphany.client.http;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import org.apiphany.ApiMimeType;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.ExchangeClient;
import org.apiphany.header.HeaderValues;
import org.apiphany.header.MapHeaderValues;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpHeaderValues;
import org.apiphany.http.HttpStatus;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Strings;
import org.apiphany.logging.Slf4jLoggerAdapter;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.lang.Pair;
import org.morphix.lang.collections.Lists;
import org.morphix.lang.function.LoggerAdapter;

/**
 * Abstract HTTP exchange client which holds all the common information needed to build an HTTP exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractHttpExchangeClient implements HttpExchangeClient {

	/**
	 * Class logger.
	 */
	private static final LoggerAdapter LOGGER = Slf4jLoggerAdapter.of(AbstractHttpExchangeClient.class);

	/**
	 * Client properties.
	 */
	private final ClientProperties clientProperties;

	/**
	 * Content converters.
	 */
	private final List<ContentConverter<?>> contentConverters = new LinkedList<>();

	/**
	 * Header values chain.
	 */
	private final HeaderValues headerValuesChain;

	/**
	 * Cached maximum allowed raw response body size in bytes.
	 */
	private final int maxResponseBodySize;

	/**
	 * Cached maximum allowed decoded response body size in bytes.
	 */
	private final int maxDecodedResponseBodySize;

	/**
	 * The SSL context for HTTPS if configured in client properties via {@link SSLProperties}.
	 */
	private final SSLContext sslContext;

	/**
	 * Initialize the client with the given client properties.
	 *
	 * @param clientProperties client properties
	 */
	protected AbstractHttpExchangeClient(final ClientProperties clientProperties) {
		LOGGER.debug("Initializing: {}", getClass().getSimpleName());
		this.clientProperties = Objects.requireNonNull(clientProperties, "clientProperties cannot be null");
		this.maxResponseBodySize = ClientProperties.Response.getMaxBodySize(clientProperties);
		this.maxDecodedResponseBodySize = ClientProperties.Response.getMaxDecodedBodySize(clientProperties);

		SSLProperties sslProperties = getCustomProperties(SSLProperties.class);
		this.sslContext = Nullables.apply(sslProperties, SSLContexts::create);

		addDefaultContentConverters(contentConverters);
		this.headerValuesChain = addDefaultHeaderValues(new HeaderValues());
	}

	/**
	 * Adds the default content converters to a given content converters list.
	 *
	 * @param contentConverters list of content converters to
	 */
	public static void addDefaultContentConverters(final List<ContentConverter<?>> contentConverters) {
		contentConverters.add(new StringHttpContentConverter());

		// TODO: abstract away the converter registration and discovery
		for (Pair<BooleanSupplier, Supplier<HttpContentConverter<?>>> converter : HttpContentConverter.JSON_CONVERTERS) {
			if (converter.left().getAsBoolean()) {
				contentConverters.add(converter.right().get());
				break;
			}
		}
	}

	/**
	 * Adds default value handlers to the header values chain.
	 *
	 * @param headerValues the header values chain object
	 * @return the header values chain with default handlers added
	 */
	public static HeaderValues addDefaultHeaderValues(final HeaderValues headerValues) {
		return headerValues
				.addFirst(new MapHeaderValues())
				.addFirst(new HttpHeaderValues());
	}

	/**
	 * Exchanges the given API request and returns the API response. This method adds common headers and tracing headers to
	 * the request before performing the exchange. It also handles any exceptions that may occur during the exchange and
	 * wraps them in an {@link HttpException} if necessary.
	 *
	 * @param <T> the type of the original request body
	 * @param <U> the target type for the response body
	 *
	 * @param apiRequest the API request to be exchanged
	 * @return the API response resulting from the exchange
	 * @throws HttpException if an error occurs during the exchange
	 * @see #doExchange(ApiRequest) for the actual exchange logic that subclasses need to implement
	 * @see ExchangeClient#exchange(ApiRequest)
	 */
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		apiRequest.addHeaders(getCommonHeaders());
		apiRequest.addHeaders(getTracingHeaders());

		return HttpException.ifThrows(() -> doExchange(apiRequest), this::customizeHttpExceptionBuilder);
	}

	/**
	 * Performs the actual exchange logic for the given API request. Subclasses need to implement this method to define how
	 * the exchange is performed using their specific underlying HTTP client. This method is called by the
	 * {@link #exchange(ApiRequest)} method after adding common headers and tracing headers to the request, and it is
	 * wrapped in a try-catch block to handle any exceptions that may occur during the exchange.
	 *
	 * @param <T> the type of the original request body
	 * @param <U> the target type for the response body
	 *
	 * @param apiRequest the API request to be exchanged
	 * @return the API response resulting from the exchange
	 */
	protected abstract <T, U> ApiResponse<U> doExchange(ApiRequest<T> apiRequest);

	/**
	 * Converts the response body to the desired type based on the request configuration and available content converters.
	 * This method:
	 * <ul>
	 * <li>Checks if the body is null or empty and returns null if so</li>
	 * <li>Checks if the body is already of the desired type and returns it if so</li>
	 * <li>Checks if the target type is String and uses the {@link StringHttpContentConverter} if so</li>
	 * <li>Iterates through registered content converters to find a compatible one</li>
	 * <li>Throws an exception if no suitable converter is found</li>
	 * </ul>
	 *
	 * @param <T> the type of the original request body
	 * @param <U> the target type for the response body
	 * @param <H> the type of response headers
	 *
	 * @param apiRequest the API request containing response type information
	 * @param mimeType the response content type
	 * @param headers the response headers
	 * @param status the HTTP status of the response
	 * @param body the raw response body to be converted
	 * @return the converted body of type {@code U}
	 * @throws UnsupportedOperationException if no compatible content converter is found
	 */
	protected <T, U, H> U convertBody(final ApiRequest<T> apiRequest, final ApiMimeType mimeType, final H headers, final HttpStatus status,
			final Object body) {
		if (isNoContent(status)) {
			return null;
		}
		if (null == body) {
			return null;
		}
		Class<?> classResponseType = apiRequest.getClassResponseType();
		if (null != classResponseType && classResponseType.isInstance(body)) {
			return JavaObjects.cast(body);
		}
		if (Objects.equals(classResponseType, String.class)) {
			return JavaObjects.cast(StringHttpContentConverter.instance().from(body, mimeType, String.class));
		}
		for (ContentConverter<?> contentConverter : getContentConverters()) {
			if (contentConverter.isConvertible(apiRequest, mimeType, headers, getHeaderValuesChain())) {
				ContentConverter<U> typeConverter = JavaObjects.cast(contentConverter);
				return ContentConverter.convertBody(typeConverter, apiRequest, mimeType, body);
			}
		}
		if (body instanceof byte[] bytes && bytes.length == 0) {
			return null;
		}
		throw new UnsupportedOperationException("No content converter found to convert response to: " + apiRequest.getResponseTypeName()
				+ ", for the response content type: " + mimeType);
	}

	/**
	 * Checks if the given HTTP status indicates that there is no content in the response. This includes both 204 No Content
	 * and 304 Not Modified statuses, as they both imply that there is no body to be processed.
	 *
	 * @param status the HTTP status to check
	 * @return true if the status indicates no content, false otherwise
	 */
	protected static boolean isNoContent(final HttpStatus status) {
		return status == HttpStatus.NO_CONTENT
				|| status == HttpStatus.NOT_MODIFIED;
	}

	/**
	 * Returns the client properties for this client.
	 *
	 * @return the client properties for this client
	 */
	@Override
	public <T extends ClientProperties> T getClientProperties() {
		return JavaObjects.cast(clientProperties);
	}

	/**
	 * Returns the content converters.
	 *
	 * @return the content converters
	 */
	public List<ContentConverter<?>> getContentConverters() { // NOSONAR the converters can have any generic type
		return contentConverters;
	}

	/**
	 * Returns the header values extractor chain.
	 *
	 * @return the header values extractor chain
	 */
	public HeaderValues getHeaderValuesChain() {
		return headerValuesChain;
	}

	/**
	 * Returns the SSL context.
	 *
	 * @return the SSL context
	 */
	@Override
	public SSLContext getSslContext() {
		return sslContext;
	}

	/**
	 * Retrieves the values of a specific header from the provided headers object using the header values chain.
	 *
	 * @param <N> header name type
	 *
	 * @param header the name of the header whose values are to be retrieved.
	 * @param headers the headers object from which to retrieve the header values.
	 * @return a list of values for the specified header. If the header is not found, an empty list is returned.
	 */
	public <N> List<String> getHeaderValues(final N header, final Object headers) {
		return getHeaderValuesChain().get(header, headers);
	}

	/**
	 * Returns true if the API request contains the "Content-Type" header with the value "application/json", false
	 * otherwise.
	 *
	 * @param <T> the type of the original request body
	 *
	 * @param apiRequest the API request to check for JSON content type
	 * @return true if the API request contains the "Content-Type" header with the value "application/json"
	 */
	protected static <T> boolean isContentJson(final ApiRequest<T> apiRequest) {
		return apiRequest.containsHeader(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON);
	}

	/**
	 * Returns the maximum allowed raw response body size in bytes.
	 *
	 * @return max response body size in bytes
	 */
	protected int getMaxResponseBodySize() {
		return maxResponseBodySize;
	}

	/**
	 * Returns the maximum allowed decoded response body size in bytes.
	 *
	 * @return max decoded response body size in bytes
	 */
	protected int getMaxDecodedResponseBodySize() {
		return maxDecodedResponseBodySize;
	}

	/**
	 * Creates an HttpException indicating that the response body exceeds the configured maximum size.
	 *
	 * @param contentLength the actual content length of the response body
	 * @param maxBodySize the configured maximum body size in bytes
	 * @return an HttpException with status {@link HttpStatus#PAYLOAD_TOO_LARGE} and a message describing the issue
	 */
	protected HttpException responseTooLargeException(final long contentLength, final int maxBodySize) {
		return new HttpException(HttpStatus.PAYLOAD_TOO_LARGE,
				"Response body exceeds configured max size: " + contentLength + " > " + maxBodySize);
	}

	/**
	 * Checks declared content-length header against configured response-size limit.
	 *
	 * @param <T> the type of headers
	 *
	 * @param headers response headers
	 * @param maxBodySize max body size in bytes
	 */
	protected <T> void ensureContentLengthWithinLimit(final T headers, final int maxBodySize) {
		List<String> contentLengthValues = getHeaderValues(HttpHeader.CONTENT_LENGTH, headers);
		if (Lists.isEmpty(contentLengthValues)) {
			return;
		}
		String contentLengthValue = contentLengthValues.getFirst();
		if (Strings.isBlank(contentLengthValue)) {
			return;
		}
		long contentLength;
		try {
			contentLength = Long.parseLong(contentLengthValue.trim());
		} catch (NumberFormatException e) {
			return;
		}
		if (contentLength > maxBodySize) {
			throw responseTooLargeException(contentLength, maxBodySize);
		}
	}

	/**
	 * Checks buffered response body size against configured response-size limit.
	 *
	 * @param <T> the type of body
	 *
	 * @param body the body to check the size of
	 * @param maxBodySize max body size in bytes
	 */
	protected <T> void ensureBodySizeWithinLimit(final T body, final int maxBodySize) {
		if (!(body instanceof byte[] bytes)) {
			return;
		}
		if (bytes.length > maxBodySize) {
			throw responseTooLargeException(bytes.length, maxBodySize);
		}
	}

	/**
	 * Builds an API response based on the given parameters. If the HTTP status indicates an error, it creates an
	 * HttpException with the error response body and includes it in the {@link ApiResponse}. Otherwise, it converts the
	 * response body to the desired type and includes it in the {@link ApiResponse}.
	 *
	 * @param <T> the type of the original request body
	 * @param <U> the target type for the response body
	 * @param <R> the type of the original response body
	 *
	 * @param apiRequest the API request associated with this response
	 * @param httpStatus the HTTP status of the response
	 * @param headers the headers of the response
	 * @param contentType the content type of the response body
	 * @param rawBody the raw response body to be converted and included in the ApiResponse
	 * @return an ApiResponse object containing either the converted response body or an exception if an error occurred
	 */
	protected <T, U, R> ApiResponse<U> buildResponse(final ApiRequest<T> apiRequest, final HttpStatus httpStatus,
			final Map<String, List<String>> headers, final HttpContentType contentType, final R rawBody) {
		ApiResponse.Builder<U> responseBuilder = ApiResponse.<U>builder()
				.status(httpStatus)
				.headers(headers)
				.request(apiRequest)
				.exchangeClient(this);
		U body;
		if (httpStatus.isError()) {
			String errorResponseBody = StringHttpContentConverter.from(rawBody, contentType);
			HttpException exception = HttpException.builder()
					.status(httpStatus)
					.responseBody(errorResponseBody)
					.build();
			responseBuilder.exception(exception);
			body = JavaObjects.cast(errorResponseBody);
		} else {
			body = convertBody(apiRequest, contentType, headers, httpStatus, rawBody);
		}
		return responseBuilder
				.body(body)
				.build();
	}

	/**
	 * Customizes the given {@link HttpException.Builder} based on the provided throwable. This method extracts the HTTP
	 * status and response body from the throwable and sets them in the builder. Subclasses can override this method to
	 * provide additional customization logic if needed.
	 *
	 * @param httpExceptionBuilder the HttpException.Builder to customize
	 * @param throwable the throwable to extract information from
	 */
	protected void customizeHttpExceptionBuilder(final HttpException.Builder httpExceptionBuilder, final Throwable throwable) {
		httpExceptionBuilder
				.status(extractHttpStatus(throwable))
				.responseBody(extractResponseBody(throwable));
	}

	/**
	 * Extracts the HTTP status from the given throwable. If the throwable is an instance of {@link HttpException}, it
	 * returns the status from the exception, otherwise, it returns {@code null}. Subclasses can override this method to
	 * provide custom logic for extracting the HTTP status from different types of exceptions.
	 *
	 * @param throwable the throwable to extract the status from
	 * @return the extracted HTTP status
	 */
	protected HttpStatus extractHttpStatus(final Throwable throwable) {
		return switch (throwable) {
			case HttpException httpException -> httpException.getStatus();
			default -> null;
		};
	}

	/**
	 * Extracts the response body from the given throwable. If the throwable is an instance of {@link HttpException}, it
	 * returns the response body from the exception, otherwise, it returns {@code null}. Subclasses can override this method
	 * to provide custom logic for extracting the response body from different types of exceptions.
	 *
	 * @param throwable the throwable to extract the response body from
	 * @return the extracted response body, or {@code null} if not applicable
	 */
	protected String extractResponseBody(final Throwable throwable) {
		return switch (throwable) {
			case HttpException httpException -> httpException.getResponseBody();
			default -> null;
		};
	}
}
