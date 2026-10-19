package org.apiphany.http;

import java.net.ProtocolException;
import java.time.Duration;
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
		builder.addInterceptor(chain -> {
			Request request = chain.request();
			if (null == request.header(HttpHeader.Name.ACCEPT_ENCODING)) {
				// any non-null value disables OkHttp's transparent gzip, keeping the raw response body intact
				Request marked = request.newBuilder()
						.header(HttpHeader.Name.ACCEPT_ENCODING, ContentEncoding.Value.IDENTITY)
						.tag(AddedAcceptEncodingMarker.class, AddedAcceptEncodingMarker.INSTANCE)
						.build();
				return chain.proceed(marked);
			}
			return chain.proceed(request);
		});
		builder.addNetworkInterceptor(chain -> {
			Request request = chain.request();
			if (null != request.tag(AddedAcceptEncodingMarker.class)) {
				// the header was added by this library, remove it so it never appears on the wire
				Request unmarked = request.newBuilder()
						.removeHeader(HttpHeader.Name.ACCEPT_ENCODING)
						.build();
				return chain.proceed(unmarked);
			}
			return chain.proceed(request);
		});
		return builder;
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
		return disableTransparentAcceptEncoding(new OkHttpClient.Builder()
				.connectTimeout(timeout.getConnect())
				.readTimeout(timeout.getSocket())
				.writeTimeout(timeout.getSocket())
				.callTimeout(timeout.getRequest())
				.connectionPool(new ConnectionPool(connection.getMaxTotal(), connection.getTimeToLive().toMillis(), TimeUnit.MILLISECONDS))
				.followRedirects(connection.isFollowRedirects())
				.followSslRedirects(connection.isFollowRedirects()));
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
	 * Tag marker used to identify requests whose {@code Accept-Encoding} header was added internally by
	 * {@link #disableTransparentAcceptEncoding(OkHttpClient.Builder)}.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	enum AddedAcceptEncodingMarker {
		/**
		 * Single marker instance.
		 */
		INSTANCE
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
