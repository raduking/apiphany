package org.apiphany.client.http;

import java.util.List;
import java.util.Map;

import org.apiphany.ApiClientFluentAdapter;
import org.apiphany.ParameterFunction;
import org.apiphany.RequestParameters;
import org.apiphany.http.HttpContentType;
import org.apiphany.http.HttpHeader;
import org.apiphany.http.HttpMethod;
import org.apiphany.io.ChunkedBinary;
import org.apiphany.io.ContentType;

/**
 * Fluent adapter for HTTP calls.
 *
 * @author Radu Sebastian LAZIN
 */
public class HttpClientFluentAdapter {

	/**
	 * The API client fluent adapter which constructed this object.
	 */
	private final ApiClientFluentAdapter apiClientFluentAdapter;

	/**
	 * The HTTP exchange client that will process the requests.
	 */
	private final HttpExchangeClient httpExchangeClient;

	/**
	 * Constructor with API client fluent adapter.
	 *
	 * @param apiClientFluentAdapter the API client fluent adapter
	 */
	protected HttpClientFluentAdapter(final ApiClientFluentAdapter apiClientFluentAdapter) {
		this.apiClientFluentAdapter = apiClientFluentAdapter;
		this.httpExchangeClient = apiClientFluentAdapter.getExchangeClient(HttpExchangeClient.class);
	}

	/**
	 * Factory method to construct the object with a given API client fluent adapter.
	 *
	 * @param apiClientFluentAdapter the underlying API client fluent adapter
	 * @return a new API client fluent adapter object
	 */
	public static HttpClientFluentAdapter of(final ApiClientFluentAdapter apiClientFluentAdapter) {
		return new HttpClientFluentAdapter(apiClientFluentAdapter);
	}

	/**
	 * Sets the request method to GET.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter get() {
		return apiClientFluentAdapter.method(httpExchangeClient.get());
	}

	/**
	 * Sets the request method to PUT.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter put() {
		return apiClientFluentAdapter.method(httpExchangeClient.put());
	}

	/**
	 * Sets the request method to POST.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter post() {
		return apiClientFluentAdapter.method(httpExchangeClient.post());
	}

	/**
	 * Sets the request method to DELETE.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter delete() {
		return apiClientFluentAdapter.method(httpExchangeClient.delete());
	}

	/**
	 * Sets the request method to PATCH.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter patch() {
		return apiClientFluentAdapter.method(httpExchangeClient.patch());
	}

	/**
	 * Sets the request method to HEAD.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter head() {
		return apiClientFluentAdapter.method(httpExchangeClient.head());
	}

	/**
	 * Sets the request method to OPTIONS.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter options() {
		return apiClientFluentAdapter.method(httpExchangeClient.options());
	}

	/**
	 * Sets the request method to TRACE.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter trace() {
		return apiClientFluentAdapter.method(httpExchangeClient.trace());
	}

	/**
	 * Sets the request method to CONNECT.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter connect() {
		return apiClientFluentAdapter.method(httpExchangeClient.connect());
	}

	/**
	 * Sets the request method to a custom value.
	 *
	 * @param method the request method
	 * @return this
	 */
	public ApiClientFluentAdapter method(final HttpMethod method) {
		return apiClientFluentAdapter.method(httpExchangeClient.method(method));
	}

	/**
	 * Sets the request body as URL-encoded form parameters. Automatically sets the {@code Content-Type} header to
	 * {@code application/x-www-form-urlencoded}.
	 *
	 * @param params the form parameters
	 * @return this
	 */
	public ApiClientFluentAdapter form(final Map<String, List<String>> params) {
		var encodedParams = RequestParameters.encode(params, apiClientFluentAdapter.getCharset());
		var body = RequestParameters.asString(encodedParams);
		return apiClientFluentAdapter.body(body)
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED);
	}

	/**
	 * Sets the request body as URL-encoded form parameters. Automatically sets the {@code Content-Type} header to
	 * {@code application/x-www-form-urlencoded}.
	 *
	 * @param paramFunctions the parameter functions
	 * @return this
	 */
	public ApiClientFluentAdapter form(final ParameterFunction... paramFunctions) {
		return form(RequestParameters.of(paramFunctions));
	}

	/**
	 * Sets the request body as a multipart form-data body. Automatically sets the {@code Content-Type} header to
	 * {@code multipart/form-data; boundary=...}.
	 *
	 * @param body the multipart body
	 * @return this
	 */
	public ApiClientFluentAdapter multipart(final ChunkedBinary body) {
		return apiClientFluentAdapter.body(body.toByteArray())
				.header(HttpHeader.CONTENT_TYPE, HttpContentType.builder()
						.contentType(ContentType.MULTIPART_FORM_DATA)
						.boundary(body.getBoundary())
						.build());
	}
}
