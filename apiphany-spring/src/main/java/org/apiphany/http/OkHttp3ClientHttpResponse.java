package org.apiphany.http;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
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
	 * Lazily populated Spring view of {@link #response} headers.
	 */
	@Nullable
	private volatile HttpHeaders headers;

	/**
	 * Constructor.
	 *
	 * @param response OkHttp response
	 */
	public OkHttp3ClientHttpResponse(final Response response) {
		Assert.notNull(response, "Response must not be null");
		this.response = response;
	}

	/**
	 * @see ClientHttpResponse#getStatusCode()
	 */
	@Override
	public HttpStatusCode getStatusCode() throws IOException {
		return HttpStatusCode.valueOf(this.response.code());
	}

	/**
	 * @see ClientHttpResponse#getStatusText()
	 */
	@Override
	public String getStatusText() {
		return this.response.message();
	}

	/**
	 * @see ClientHttpResponse#getBody()
	 */
	@Override
	public InputStream getBody() throws IOException {
		ResponseBody body = this.response.body();
		return (body != null ? body.byteStream() : InputStream.nullInputStream());
	}

	/**
	 * @see ClientHttpResponse#getHeaders()
	 */
	@Override
	public HttpHeaders getHeaders() {
		HttpHeaders headers = this.headers;
		if (headers == null) {
			headers = new HttpHeaders();
			for (String headerName : this.response.headers().names()) {
				for (String headerValue : this.response.headers(headerName)) {
					headers.add(headerName, headerValue);
				}
			}
			this.headers = headers;
		}
		return headers;
	}

	/**
	 * @see ClientHttpResponse#close()
	 */
	@Override
	public void close() {
		ResponseBody body = this.response.body();
		if (body != null) {
			body.close();
		}
	}
}
