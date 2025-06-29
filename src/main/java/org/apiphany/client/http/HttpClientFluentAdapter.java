package org.apiphany.client.http;

import org.apiphany.ApiClientFluentAdapter;

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
	 * Sets the request method to TRACE.
	 *
	 * @return this
	 */
	public ApiClientFluentAdapter trace() {
		return apiClientFluentAdapter.method(httpExchangeClient.trace());
	}

}
