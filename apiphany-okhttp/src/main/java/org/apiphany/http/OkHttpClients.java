package org.apiphany.http;

import java.io.IOException;
import java.net.ProtocolException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.OkHttpProperties;
import org.apiphany.security.ssl.HostnameVerifiers;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.morphix.lang.Throwables;
import org.morphix.lang.function.Consumers;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Interface containing utility methods for creating/configuring OkHttp clients.
 *
 * @author Radu Sebastian LAZIN
 */
public interface OkHttpClients {

	/**
	 * Returns true if the given HTTP method requires a request body, i.e. OkHttp rejects the request without one: POST, PUT
	 * and PATCH. For those requests an empty body is sent so the request still carries {@code Content-Length: 0}.
	 *
	 * @param httpMethod HTTP method
	 * @return true if the method requires a request body, false otherwise
	 */
	static boolean requiresRequestBody(final HttpMethod httpMethod) {
		return requiresRequestBody(httpMethod.value());
	}

	/**
	 * Returns true if the given HTTP method requires a request body, i.e. OkHttp rejects the request without one: POST, PUT
	 * and PATCH. For those requests an empty body is sent so the request still carries {@code Content-Length: 0}.
	 *
	 * @param httpMethod HTTP method
	 * @return true if the method requires a request body, false otherwise
	 */
	static boolean requiresRequestBody(final String httpMethod) {
		return switch (httpMethod) {
			case HttpMethod.Value.POST, HttpMethod.Value.PUT, HttpMethod.Value.PATCH -> true;
			default -> false;
		};
	}

	/**
	 * Disables OkHttp's transparent handling of the given header by temporarily adding it to requests that do not already
	 * contain it, then removing it before the request is written to the network.
	 * <p>
	 * The temporary header value and request marker are added by an application interceptor. A network interceptor removes
	 * the header only when it was added by this method, preserving headers explicitly set by the caller.
	 *
	 * @param builder the OkHttp client builder to configure
	 * @param header the HTTP header whose transparent handling should be disabled
	 * @param temporaryValue the temporary value used to disable the transparent handling
	 * @return the same builder instance
	 */
	static OkHttpClient.Builder disableTransparentHeader(final OkHttpClient.Builder builder, final HttpHeader header, final String temporaryValue) {
		builder.addInterceptor(chain -> {
			Request request = chain.request();
			if (null == request.header(header.value())) {
				// prevent OkHttp from automatically handling the header
				Request marked = request.newBuilder()
						.header(header.value(), temporaryValue)
						.tag(HttpHeader.class, header)
						.build();
				return chain.proceed(marked);
			}
			return chain.proceed(request);
		});
		builder.addNetworkInterceptor(chain -> {
			Request request = chain.request();
			HttpHeader marker = request.tag(HttpHeader.class);
			if (header == marker) {
				// the header was added by this library, remove it so it never appears on the wire
				Request unmarked = request.newBuilder()
						.removeHeader(header.value())
						.build();
				return chain.proceed(unmarked);
			}
			return chain.proceed(request);
		});
		return builder;
	}

	/**
	 * Disables OkHttp's transparent gzip: OkHttp silently adds an {@code Accept-Encoding: gzip} request header and
	 * transparently de-compresses responses, hiding the {@code Content-Encoding} header from the caller.
	 * <p>
	 * Apiphany never adds headers the caller did not set explicitly, and it decodes {@code Content-Encoding} itself, so
	 * this is done with a request/response interceptor pair. The internal header is added after the caller's headers and
	 * removed before the request is written to the network, so it never appears on the wire.
	 *
	 * @param builder the OkHttp client builder to configure
	 * @return the same builder instance
	 */
	static OkHttpClient.Builder disableTransparentAcceptEncoding(final OkHttpClient.Builder builder) {
		// any non-null value disables OkHttp's transparent gzip, keeping the raw response body intact
		return disableTransparentHeader(builder, HttpHeader.ACCEPT_ENCODING, ContentEncoding.Value.IDENTITY);
	}

	/**
	 * Returns a configured OkHttp client based on the given client properties. The caller is responsible for shutting down
	 * the returned client.
	 *
	 * @param clientProperties HTTP client properties
	 * @return OkHttp client
	 */
	static OkHttpClient createClient(final ClientProperties clientProperties) {
		return createClient(clientProperties, noCustomizer());
	}

	/**
	 * Returns a configured OkHttp client based on the given client properties. The caller is responsible for shutting down
	 * the returned client.
	 *
	 * @param clientProperties HTTP client properties
	 * @param builderCustomizer builder customizer
	 * @return OkHttp client
	 */
	static OkHttpClient createClient(
			final ClientProperties clientProperties,
			final Consumer<OkHttpClient.Builder> builderCustomizer) {
		OkHttpProperties okHttpProperties = clientProperties.getCustomProperties(OkHttpProperties.class);

		OkHttpClient.Builder builder = null == okHttpProperties
				? createBuilder(clientProperties)
				: createBuilder(okHttpProperties);
		builderCustomizer.accept(builder);

		return builder.build();
	}

	/**
	 * Returns an {@link OkHttpClient.Builder} configured with the given generic client properties.
	 *
	 * @param clientProperties HTTP client properties
	 * @return an OkHttp client builder
	 */
	static OkHttpClient.Builder createBuilder(final ClientProperties clientProperties) {
		ClientProperties.Timeout timeout = clientProperties.getTimeout();
		ClientProperties.Connection connection = clientProperties.getConnection();
		return disableTransparentAcceptEncoding(new OkHttpClient.Builder())
				.connectTimeout(timeout.getConnect())
				.readTimeout(timeout.getSocket())
				.writeTimeout(timeout.getSocket())
				.callTimeout(timeout.getRequest())
				.connectionPool(new ConnectionPool(connection.getMaxTotal(), connection.getTimeToLive().toMillis(), TimeUnit.MILLISECONDS))
				.followRedirects(connection.isFollowRedirects())
				.followSslRedirects(connection.isFollowRedirects());
	}

	/**
	 * Returns an {@link OkHttpClient.Builder} configured with the given OkHttp specific properties.
	 *
	 * @param okHttpProperties OkHttp properties
	 * @return an OkHttp client builder
	 */
	static OkHttpClient.Builder createBuilder(final OkHttpProperties okHttpProperties) {
		Duration keepAliveDuration = okHttpProperties.getConnection().getKeepAliveDuration();
		return disableTransparentAcceptEncoding(new OkHttpClient.Builder()
				.connectTimeout(okHttpProperties.getConnect().getTimeout())
				.readTimeout(okHttpProperties.getRead().getTimeout())
				.writeTimeout(okHttpProperties.getWrite().getTimeout())
				.callTimeout(okHttpProperties.getRequest().getCallTimeout())
				.connectionPool(new ConnectionPool(
						okHttpProperties.getConnection().getMaxIdleConnections(),
						keepAliveDuration.toMillis(),
						TimeUnit.MILLISECONDS))
				.followRedirects(okHttpProperties.getConnection().isFollowRedirects())
				.followSslRedirects(okHttpProperties.getConnection().isFollowRedirects()));
	}

	/**
	 * Configures the given OkHttp client builder with the given SSL context.
	 * <p>
	 * OkHttp requires both an {@link javax.net.ssl.SSLSocketFactory} and the {@link X509TrustManager} it was built with.
	 * Since a {@link SSLContext} does not expose the trust managers it was initialized with, the trust manager is rebuilt
	 * from the truststore configuration when available, and the platform default trust manager is used otherwise. The
	 * actual TLS handshake trust decision is enforced by the socket factory created from the given {@code sslContext}.
	 *
	 * @param builder the OkHttp client builder to configure
	 * @param sslContext the SSL context to use for TLS connections
	 * @param sslProperties the SSL properties the given context was created from, may be null
	 */
	static void configureTls(final OkHttpClient.Builder builder, final SSLContext sslContext, final SSLProperties sslProperties) {
		X509TrustManager trustManager = null == sslProperties || null == sslProperties.getTruststore()
				? SSLContexts.defaultTrustManager(X509TrustManager.class)
				: SSLContexts.trustManager(X509TrustManager.class, SSLContexts.trustManagers(sslProperties.getTruststore()));
		configureTls(builder, sslContext, trustManager);
	}

	/**
	 * Configures the given OkHttp client builder with the given SSL context and X.509 trust manager.
	 * <p>
	 * OkHttp requires the trust manager used to create the SSL socket factory to be supplied alongside that factory. The
	 * hostname verifier is also configured to support subjectAltNames with certificate common-name fallback.
	 *
	 * @param builder the OkHttp client builder to configure
	 * @param sslContext the SSL context to use for TLS connections
	 * @param trustManager the X.509 trust manager used with the SSL context
	 */
	static void configureTls(final OkHttpClient.Builder builder, final SSLContext sslContext, final X509TrustManager trustManager) {
		builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
		// OkHttp's own verifier requires subjectAltNames, this one falls back to the certificate CN like Apache HC5's default
		builder.hostnameVerifier(HostnameVerifiers.defaultVerifier());
	}

	/**
	 * Returns true if the given throwable is the OkHttp circular redirect (too many follow-up requests) exception type.
	 *
	 * @param throwable throwable to inspect
	 * @return true if throwable matches OkHttp's circular redirect exception type
	 */
	static boolean isCircularRedirectException(final Throwable throwable) {
		return Throwables.anyMatch(throwable, t -> t instanceof ProtocolException
				&& null != t.getMessage()
				&& t.getMessage().startsWith("Too many follow-up requests"));
	}

	/**
	 * Shuts down the dispatcher, evicts the connection pool, and closes the cache of the given OkHttp client. The caller
	 * should invoke this when the client is no longer needed.
	 *
	 * @param httpClient OkHttp client to shut down
	 * @throws IOException when any error occurs during shutdown
	 */
	static void shutdown(final OkHttpClient httpClient) throws IOException {
		@SuppressWarnings("resource")
		ExecutorService executor = httpClient.dispatcher().executorService();
		executor.shutdown();
		httpClient.connectionPool().evictAll();
		try (Cache cache = httpClient.cache()) {
			// empty
		}
	}

	/**
	 * To be used in conjunction with {@link #createClient(ClientProperties, Consumer)} when no customization is necessary.
	 *
	 * @param <T> customizer type
	 *
	 * @return empty customizer
	 */
	static <T> Consumer<T> noCustomizer() {
		return Consumers.noConsumer();
	}
}
