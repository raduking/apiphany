package org.apiphany.http;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.Assert;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * {@link ClientHttpRequestFactory} implementation that uses OkHttp as the HTTP engine.
 * <p>
 * Adapted from Spring Framework's {@code org.springframework.http.client.OkHttp3ClientHttpRequestFactory}. Spring
 * Framework 7 removes this factory from {@code spring-web}; Apiphany provides a local copy so
 * {@link CloseableHttpRequestFactory} and Spring REST clients can keep using OkHttp when
 * {@link org.apiphany.client.http.SpringRestClientProperties} selects the {@code okhttp} client library.
 * <p>
 * When constructed with the no-arg constructor, {@link #destroy()} shuts down the internally created
 * {@link OkHttpClient}. When constructed with a caller-supplied client, lifecycle remains the caller's responsibility
 * (as with {@link CloseableHttpRequestFactory.OkHttp#create(org.apiphany.client.ClientProperties, java.util.Map)}).
 *
 * @author Radu Sebastian LAZIN
 */
public class OkHttp3ClientHttpRequestFactory implements ClientHttpRequestFactory, DisposableBean {

	/**
	 * OkHttp client used to execute requests.
	 */
	private OkHttpClient client;

	/**
	 * Whether {@link #client} was created by this factory's no-arg constructor.
	 */
	private final boolean defaultClient;

	/**
	 * Creates a factory with a default {@link OkHttpClient} instance.
	 */
	public OkHttp3ClientHttpRequestFactory() {
		this.client = new OkHttpClient();
		this.defaultClient = true;
	}

	/**
	 * Creates a factory with the given {@link OkHttpClient} instance.
	 *
	 * @param client the client to use
	 */
	public OkHttp3ClientHttpRequestFactory(final OkHttpClient client) {
		Assert.notNull(client, "OkHttpClient must not be null");
		this.client = client;
		this.defaultClient = false;
	}

	/**
	 * Sets the underlying read timeout in milliseconds. A value of {@code 0} specifies an infinite timeout.
	 *
	 * @param readTimeout read timeout in milliseconds
	 */
	public void setReadTimeout(final int readTimeout) {
		this.client = this.client.newBuilder()
				.readTimeout(readTimeout, TimeUnit.MILLISECONDS)
				.build();
	}

	/**
	 * Sets the underlying read timeout. A zero duration specifies an infinite timeout.
	 *
	 * @param readTimeout read timeout
	 */
	public void setReadTimeout(final Duration readTimeout) {
		this.client = this.client.newBuilder()
				.readTimeout(readTimeout)
				.build();
	}

	/**
	 * Sets the underlying write timeout in milliseconds. A value of {@code 0} specifies an infinite timeout.
	 *
	 * @param writeTimeout write timeout in milliseconds
	 */
	public void setWriteTimeout(final int writeTimeout) {
		this.client = this.client.newBuilder()
				.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
				.build();
	}

	/**
	 * Sets the underlying write timeout. A zero duration specifies an infinite timeout.
	 *
	 * @param writeTimeout write timeout
	 */
	public void setWriteTimeout(final Duration writeTimeout) {
		this.client = this.client.newBuilder()
				.writeTimeout(writeTimeout)
				.build();
	}

	/**
	 * Sets the underlying connect timeout in milliseconds. A value of {@code 0} specifies an infinite timeout.
	 *
	 * @param connectTimeout connect timeout in milliseconds
	 */
	public void setConnectTimeout(final int connectTimeout) {
		this.client = this.client.newBuilder()
				.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
				.build();
	}

	/**
	 * Sets the underlying connect timeout. A zero duration specifies an infinite timeout.
	 *
	 * @param connectTimeout connect timeout
	 */
	public void setConnectTimeout(final Duration connectTimeout) {
		this.client = this.client.newBuilder()
				.connectTimeout(connectTimeout)
				.build();
	}

	/**
	 * @see ClientHttpRequestFactory#createRequest(URI, HttpMethod)
	 */
	@Override
	public ClientHttpRequest createRequest(final URI uri, final HttpMethod httpMethod) {
		return new OkHttp3ClientHttpRequest(this.client, uri, httpMethod);
	}

	/**
	 * Shuts down resources owned by a {@link OkHttpClient} created in the no-arg constructor.
	 *
	 * @see DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws IOException {
		if (this.defaultClient) {
			Cache cache = this.client.cache();
			if (cache != null) {
				cache.close();
			}
			this.client.dispatcher().executorService().shutdown();
			this.client.connectionPool().evictAll();
		}
	}

}
