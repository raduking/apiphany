package org.apiphany.http;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * {@link ClientHttpResponse} adapter for an OkHttp {@link Response}.
 * <p>
 * Adapted from Spring Framework's {@code org.springframework.http.client.OkHttp3ClientHttpResponse}. Spring Framework 7
 * removes the OkHttp client types from {@code spring-web}; Apiphany keeps this adapter so
 * {@link OkHttp3ClientHttpRequest} can integrate with {@link org.springframework.web.client.RestTemplate} and
 * {@link org.springframework.web.client.RestClient} unchanged.
 *
 * @author Radu Sebastian LAZIN
 */
public class OkHttp3ClientHttpResponse implements ClientHttpResponse {

	/**
	 * The underlying OkHttp response.
	 */
	private final Response response;

	/**
	 * Spring view of {@link #response} headers.
	 */
	private final HttpHeaders headers;

	/**
	 * Constructor.
	 *
	 * @param response OkHttp response
	 */
	public OkHttp3ClientHttpResponse(final Response response) {
		Assert.notNull(response, "Response must not be null");
		this.response = response;
		this.headers = createHeaders(response);
	}

	/**
	 * @see ClientHttpResponse#getStatusCode()
	 */
	@Override
	public HttpStatusCode getStatusCode() throws IOException {
		return HttpStatusCode.valueOf(response.code());
	}

	/**
	 * @see ClientHttpResponse#getStatusText()
	 */
	@Override
	public String getStatusText() {
		return response.message();
	}

	/**
	 * @see ClientHttpResponse#getBody()
	 */
	@Override
	@SuppressWarnings("resource")
	public InputStream getBody() throws IOException {
		ResponseBody body = response.body();
		if (null == body) {
			return InputStream.nullInputStream();
		}
		return body.byteStream();
	}

	/**
	 * @see ClientHttpResponse#getHeaders()
	 */
	@Override
	public HttpHeaders getHeaders() {
		return headers;
	}

	/**
	 * Creates a Spring view of the given OkHttp response headers.
	 *
	 * @param response OkHttp response
	 * @return Spring HTTP headers
	 */
	private static HttpHeaders createHeaders(final Response response) {
		HttpHeaders result = new HttpHeaders();
		for (String headerName : response.headers().names()) {
			for (String headerValue : response.headers(headerName)) {
				result.add(headerName, headerValue);
			}
		}
		return result;
	}

	/**
	 * @see ClientHttpResponse#close()
	 */
	@Override
	public void close() {
		response.close();
	}
}
