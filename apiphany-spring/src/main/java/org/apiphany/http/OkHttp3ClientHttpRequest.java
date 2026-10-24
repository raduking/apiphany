package org.apiphany.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apiphany.lang.Bytes;
import org.morphix.lang.Nullables;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * {@link ClientHttpRequest} implementation that executes exchanges through OkHttp.
 * <p>
 * Adapted from Spring Framework's {@code org.springframework.http.client.OkHttp3ClientHttpRequest}. Spring Framework 7
 * removes the OkHttp client types from {@code spring-web}; Apiphany keeps this request type for use with
 * {@link OkHttp3ClientHttpRequestFactory} and {@link CloseableHttpRequestFactory.OkHttp}.
 *
 * @author Radu Sebastian LAZIN
 */
public class OkHttp3ClientHttpRequest extends AbstractStreamingClientHttpRequest {

	/**
	 * OkHttp client that executes the request.
	 */
	private final OkHttpClient client;

	/**
	 * Request URI.
	 */
	private final URI uri;

	/**
	 * HTTP method.
	 */
	private final HttpMethod method;

	/**
	 * Constructor.
	 *
	 * @param client OkHttp client
	 * @param uri request URI
	 * @param method HTTP method
	 */
	public OkHttp3ClientHttpRequest(final OkHttpClient client, final URI uri, final HttpMethod method) {
		this.client = client;
		this.uri = uri;
		this.method = method;
	}

	/**
	 * @see ClientHttpRequest#getMethod()
	 */
	@Override
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * @see ClientHttpRequest#getURI()
	 */
	@Override
	public URI getURI() {
		return uri;
	}

	/**
	 * Returns the client set for this request.
	 *
	 * @return the client set for this request
	 */
	protected OkHttpClient getClient() {
		return client;
	}

	/**
	 * @see AbstractStreamingClientHttpRequest#executeInternal(HttpHeaders, Body)
	 */
	@Override
	@SuppressWarnings("resource")
	protected ClientHttpResponse executeInternal(final HttpHeaders headers, final Body body) throws IOException {
		RequestBody requestBody = null;
		String httpMethod = getMethod().name();
		if (null != body) {
			requestBody = new BodyRequestBody(headers, body);
		} else if (OkHttpClients.requiresRequestBody(httpMethod)) {
			String header = headers.getFirst(HttpHeaders.CONTENT_TYPE);
			MediaType contentType = null != header ? MediaType.parse(header) : null;
			requestBody = RequestBody.create(Bytes.EMPTY, contentType);
		}
		Request.Builder builder = new Request.Builder()
				.url(getURI().toURL())
				.method(httpMethod, requestBody);
		headers.forEach((headerName, headerValues) -> {
			for (String headerValue : headerValues) {
				builder.addHeader(headerName, Nullables.nonNullOrDefault(headerValue, ""));
			}
		});
		Request request = builder.build();
		Response response = client.newCall(request).execute();
		return new OkHttp3ClientHttpResponse(response);
	}

	/**
	 * {@link RequestBody} that writes a Spring {@link StreamingHttpOutputMessage.Body} and reflects {@code Content-Length}
	 * and {@code Content-Type} from {@link HttpHeaders}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static class BodyRequestBody extends RequestBody {

		/**
		 * Headers used for content length and type.
		 */
		private final HttpHeaders headers;

		/**
		 * Streaming body to write on the wire.
		 */
		private final Body body;

		/**
		 * Constructor.
		 *
		 * @param headers request headers
		 * @param body streaming body
		 */
		BodyRequestBody(final HttpHeaders headers, final Body body) {
			this.headers = headers;
			this.body = body;
		}

		/**
		 * @see RequestBody#contentLength()
		 */
		@Override
		public long contentLength() {
			return headers.getContentLength();
		}

		/**
		 * @see RequestBody#contentType()
		 */
		@Override
		public MediaType contentType() {
			String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
			if (StringUtils.hasText(contentType)) {
				return MediaType.parse(contentType);
			}
			return null;
		}

		/**
		 * @see RequestBody#writeTo(BufferedSink)
		 */
		@Override
		@SuppressWarnings("resource")
		public void writeTo(final BufferedSink sink) throws IOException {
			OutputStream os = sink.outputStream();
			body.writeTo(os);
		}

		/**
		 * @see RequestBody#isOneShot()
		 */
		@Override
		public boolean isOneShot() {
			return !body.repeatable();
		}
	}
}
