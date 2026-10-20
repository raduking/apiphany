package org.apiphany.client.http;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.ContentEncoding;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpException;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.http.OkHttpClients;
import org.apiphany.http.OkHttpEntities;
import org.apiphany.http.OkHttpLibrary;
import org.apiphany.io.IOStreams;
import org.apiphany.io.InputStreamSupplier;
import org.apiphany.lang.Bytes;
import org.apiphany.security.ssl.SSLProperties;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.lang.collections.Lists;
import org.morphix.lang.collections.Maps;
import org.morphix.lang.function.Predicates;
import org.morphix.lang.function.ThrowingSupplier;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OkHttp exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public class OkHttpExchangeClient extends AbstractHttpExchangeClient {

	/**
	 * Empty request body used for methods that require one (POST/PUT/PATCH) when the API request has no body. An empty byte
	 * body sends {@code Content-Length: 0} and no {@code Content-Type} header.
	 */
	private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(Bytes.EMPTY, null);

	/**
	 * The OkHttp client instance.
	 */
	private final OkHttpClient httpClient;

	/**
	 * Constructs the exchange client.
	 */
	public OkHttpExchangeClient() {
		this(ClientProperties.defaults());
	}

	/**
	 * Constructs the exchange client.
	 *
	 * @param clientProperties the client properties
	 */
	public OkHttpExchangeClient(final ClientProperties clientProperties) {
		super(clientProperties);
		OkHttpLibrary.verifyVersion();
		this.httpClient = OkHttpClients.createClient(clientProperties, this::customize);
	}

	/**
	 * Customizes the OkHttp client builder.
	 *
	 * @param builder the OkHttp client builder
	 */
	private void customize(final OkHttpClient.Builder builder) {
		SSLContext sslContext = getSslContext();
		SSLProperties sslProperties = getCustomProperties(SSLProperties.class);
		Nullables.whenNotNull(sslContext)
				.then(ssl -> OkHttpClients.configureTls(builder, ssl, sslProperties));
	}

	/**
	 * @see #close()
	 */
	@Override
	public void close() throws Exception {
		OkHttpClients.shutdown(httpClient);
	}

	/**
	 * @see AbstractHttpExchangeClient#doExchange(ApiRequest)
	 */
	@Override
	protected <T, U> ApiResponse<U> doExchange(final ApiRequest<T> apiRequest) {
		Request request = buildRequest(apiRequest);
		Call call = httpClient.newCall(request);
		if (apiRequest.isStream()) {
			return sendStreamRequest(apiRequest, call);
		}
		return sendRequest(apiRequest, call);
	}

	/**
	 * Sends a streaming HTTP request and keeps the HTTP response open so the caller can consume the body stream.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest API request object
	 * @param call the OkHttp call to execute
	 * @return API response object
	 */
	@SuppressWarnings("resource")
	protected <U, T> ApiResponse<U> sendStreamRequest(final ApiRequest<T> apiRequest, final Call call) {
		return ThrowingSupplier.<ApiResponse<U>>unchecked(() -> {
			Response response = call.execute();
			try {
				return JavaObjects.cast(buildResponse(apiRequest, response));
			} catch (Exception e) {
				response.close();
				throw e;
			}
		}).get();
	}

	/**
	 * Sends the HTTP request and returns the API response.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest API request object
	 * @param call the OkHttp call to execute
	 * @return API response object
	 */
	protected <U, T> ApiResponse<U> sendRequest(final ApiRequest<T> apiRequest, final Call call) {
		return ThrowingSupplier.<ApiResponse<U>>unchecked(() -> {
			try (Response response = call.execute()) {
				return buildResponse(apiRequest, response);
			}
		}).get();
	}

	/**
	 * @see AbstractHttpExchangeClient#getRedirectLoopFailurePredicate()
	 */
	@Override
	protected Predicate<Throwable> getRedirectLoopFailurePredicate() {
		return Predicates.anyOf(OkHttpClients::isCircularRedirectException, super.getRedirectLoopFailurePredicate());
	}

	/**
	 * @see AbstractHttpExchangeClient#extractHttpStatus(Throwable)
	 */
	@Override
	protected HttpStatus extractHttpStatus(final Throwable throwable) {
		if (getClientProperties().getConnection().isFollowRedirects()
				&& OkHttpClients.isCircularRedirectException(throwable)) {
			return HttpStatus.FOUND;
		}
		return super.extractHttpStatus(throwable);
	}

	/**
	 * Builds the OkHttp request object.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest the API request object
	 * @return an OkHttp request
	 */
	protected <T> Request buildRequest(final ApiRequest<T> apiRequest) {
		Request.Builder requestBuilder = new Request.Builder().url(apiRequest.getUri().toString());
		addHeaders(requestBuilder, apiRequest.getHeaders());

		HttpMethod httpMethod = apiRequest.getMethod();
		RequestBody requestBody = apiRequest.hasBody() ? createRequestBody(apiRequest) : null;
		if (null == requestBody && OkHttpClients.requiresRequestBody(httpMethod)) {
			// OkHttp rejects POST/PUT/PATCH without a body, an empty body sends "Content-Length: 0" and no "Content-Type"
			requestBody = EMPTY_REQUEST_BODY;
		}
		requestBuilder.method(httpMethod.toString(), requestBody);

		return requestBuilder.build();
	}

	/**
	 * Creates an appropriate {@link RequestBody} based on the request body type and headers.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest API request object
	 * @return request body object
	 */
	protected <T> RequestBody createRequestBody(final ApiRequest<T> apiRequest) {
		T body = apiRequest.getBody();
		List<String> contentTypeValues = getHeaderValues(HttpHeader.CONTENT_TYPE, apiRequest.getHeaders());
		String contentTypeValue = Lists.first(contentTypeValues);
		MediaType mediaType = toMediaType(contentTypeValue);
		return createRequestBody(apiRequest, body, mediaType);
	}

	/**
	 * Parses the given content type value to a {@link MediaType}. The value is used as-is, no charset is added: Apiphany
	 * never adds headers the caller did not set explicitly.
	 *
	 * @param contentTypeValue the raw content type header value, may be null
	 * @return the parsed media type, or null if the content type value is null or cannot be parsed
	 */
	private static MediaType toMediaType(final String contentTypeValue) {
		return null == contentTypeValue ? null : MediaType.parse(contentTypeValue);
	}

	/**
	 * Creates an appropriate {@link RequestBody} based on the request body type and content type.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest API request object
	 * @param body request body
	 * @param mediaType content type of the request body
	 * @return request body object
	 */
	private <T> RequestBody createRequestBody(final ApiRequest<T> apiRequest, final T body, final MediaType mediaType) {
		Charset charset = charset(apiRequest, mediaType);
		return HttpException.ifThrows(() -> switch (body) {
			// encoded to bytes here because OkHttp rewrites string bodies appending "; charset=utf-8" to the content type
			case String str -> RequestBody.create(str.getBytes(charset), mediaType);
			case byte[] bytes -> RequestBody.create(bytes, mediaType);
			case InputStream is -> OkHttpEntities.create(is, mediaType);
			case InputStreamSupplier iss -> OkHttpEntities.create(iss.get(), mediaType);
			case Supplier<?> supplier -> createRequestBody(apiRequest, JavaObjects.cast(supplier.get()), mediaType);
			case File file -> RequestBody.create(file, mediaType);
			case Path path -> RequestBody.create(path.toFile(), mediaType);
			case Object obj when isContentJson(apiRequest) -> RequestBody.create(toJson(apiRequest, obj).getBytes(charset), mediaType);
			case Serializable serializable -> RequestBody.create(IOStreams.toByteArray(serializable), mediaType);
			default -> RequestBody.create(fallbackToString(body).getBytes(charset), mediaType);
		});
	}

	/**
	 * Returns the charset to encode string request bodies: the media type charset when set, the request charset otherwise.
	 *
	 * @param apiRequest API request object
	 * @param mediaType content type of the request body, may be null
	 * @return the charset to encode string request bodies with
	 */
	private static Charset charset(final ApiRequest<?> apiRequest, final MediaType mediaType) {
		Charset mediaTypeCharset = null;
		if (null != mediaType) {
			mediaTypeCharset = mediaType.charset(null);
		}
		return Nullables.nonNullOrDefault(mediaTypeCharset,
				() -> Nullables.nonNullOrDefault(apiRequest.getCharset(), StandardCharsets.UTF_8));
	}

	/**
	 * Builds the API response object.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest API request object
	 * @param response OkHttp response
	 * @return API response object
	 */
	protected <T, U> ApiResponse<U> buildResponse(final ApiRequest<T> apiRequest, final Response response) {
		HttpStatus httpStatus = HttpStatus.fromCode(response.code());
		Map<String, List<String>> headers = response.headers().toMultimap();
		ensureContentLengthWithinLimit(headers, getMaxResponseBodySize());

		Object responseBody = getResponseBody(apiRequest, response);

		List<String> encodings = getHeaderValues(HttpHeader.CONTENT_ENCODING, headers);
		List<ContentEncoding> contentEncodings = ContentEncoding.parseAll(encodings);
		U decodedBody = ContentEncoding.decodeBody(JavaObjects.cast(responseBody), contentEncodings, getMaxDecodedResponseBodySize());

		List<String> contentTypes = getHeaderValues(HttpHeader.CONTENT_TYPE, headers);
		HttpContentType contentType = HttpContentType.parse(contentTypes);

		return buildResponse(apiRequest, httpStatus, headers, contentType, decodedBody);
	}

	/**
	 * Returns the response body converted to the target type. If the request is a stream, the response body is returned as
	 * an input stream, otherwise it is returned as a byte array.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest API request object
	 * @param response OkHttp response
	 * @return the response body converted to the target type
	 */
	@SuppressWarnings("resource")
	protected <T, U> U getResponseBody(final ApiRequest<T> apiRequest, final Response response) {
		Object body;
		if (apiRequest.isStream()) {
			body = OkHttpEntities.toInputStream(response);
		} else {
			body = OkHttpEntities.toByteArray(response, getMaxResponseBodySize());
		}
		return JavaObjects.cast(body);
	}

	/**
	 * Adds the given headers to the {@link Request.Builder}.
	 *
	 * @param requestBuilder builder to add the headers to
	 * @param headers map of headers to add to the request
	 */
	public static void addHeaders(final Request.Builder requestBuilder, final Map<String, List<String>> headers) {
		Maps.safe(headers)
				.forEach((headerName, headerValues) -> Lists.safe(headerValues)
						.forEach(headerValue -> requestBuilder.addHeader(headerName, headerValue)));
	}

	/**
	 * Transforms a {@link URI} to an OkHttp request builder pre-populated with the URL.
	 *
	 * @param uri URI
	 * @return a new request builder
	 */
	public static Request.Builder toRequestBuilder(final URI uri) {
		return new Request.Builder().url(uri.toString());
	}

	/**
	 * Returns the underlying OkHttp client.
	 *
	 * @return the underlying OkHttp client
	 */
	public OkHttpClient getHttpClient() {
		return httpClient;
	}
}
