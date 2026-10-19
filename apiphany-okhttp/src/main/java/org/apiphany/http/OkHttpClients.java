package org.apiphany.http;

import java.net.ProtocolException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.OkHttpProperties;
import org.apiphany.security.ssl.SSLContexts;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.StoreInfo;
import org.morphix.lang.Throwables;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.function.ThrowingSupplier;

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
	 * subjectAltName type value for dNSName entries.
	 */
	int DNS_NAME_TYPE = 2;

	/**
	 * subjectAltName type value for iPAddress entries.
	 */
	int IP_ADDRESS_TYPE = 7;

	/**
	 * Disables OkHttp's transparent gzip: OkHttp silently adds an {@code Accept-Encoding: gzip} request header and
	 * transparently decompresses responses, hiding the {@code Content-Encoding} header from the caller. Apiphany never adds
	 * headers the caller did not set explicitly, and it decodes {@code Content-Encoding} itself, so this is done with a
	 * request/response interceptor pair. The internal header is added after the caller's headers and removed before the
	 * request is written to the network, so it never appears on the wire.
	 *
	 * @param builder the OkHttp client builder to configure
	 * @return the same builder instance
	 */
	static OkHttpClient.Builder disableTransparentGzip(final OkHttpClient.Builder builder) {
		builder.addInterceptor(chain -> {
			Request request = chain.request();
			if (null == request.header(HttpHeader.ACCEPT_ENCODING.value())) {
				// any non-null value disables OkHttp's transparent gzip, keeping the raw response body intact
				Request marked = request.newBuilder()
						.header(HttpHeader.ACCEPT_ENCODING.value(), "identity")
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
						.removeHeader(HttpHeader.ACCEPT_ENCODING.value())
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
		return disableTransparentGzip(new OkHttpClient.Builder()
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
		return disableTransparentGzip(new OkHttpClient.Builder()
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
	 * {@link #disableTransparentGzip(OkHttpClient.Builder)}.
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
				? defaultTrustManager()
				: x509TrustManager(trustManagers(sslProperties.getTruststore()));
		builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
		// OkHttp's own verifier requires subjectAltNames, this one falls back to the certificate CN like Apache HC5's default
		builder.hostnameVerifier(defaultHostnameVerifier());
	}

	/**
	 * Returns a hostname verifier that matches subjectAltNames first and falls back to the certificate common name,
	 * mirroring the behavior of Apache HttpClient 5's default hostname verifier. OkHttp's own verifier rejects certificates
	 * without subjectAltNames, which is not what the other supported clients do.
	 *
	 * @return a hostname verifier with CN fallback
	 */
	static HostnameVerifier defaultHostnameVerifier() {
		return (hostname, session) -> ThrowingSupplier.unchecked(() -> {
			Certificate[] certificateChain = session.getPeerCertificates();
			if (0 == certificateChain.length || !(certificateChain[0] instanceof X509Certificate certificate)) {
				return false;
			}
			return matchesSubjectAltName(hostname, certificate) || matchesCommonName(hostname, certificate);
		}).get();
	}

	/**
	 * Returns true if the given hostname matches one of the certificate's subjectAltNames, false otherwise.
	 *
	 * @param hostname the hostname to verify
	 * @param certificate the certificate to check
	 * @return true if the hostname matches a subjectAltName, false otherwise
	 * @throws CertificateParsingException when parsing the certificate fails
	 */
	static boolean matchesSubjectAltName(final String hostname, final X509Certificate certificate) throws CertificateParsingException {
		Collection<List<?>> subjectAltNames = certificate.getSubjectAlternativeNames();
		if (null == subjectAltNames) {
			return false;
		}
		for (List<?> subjectAltName : subjectAltNames) {
			if (2 > subjectAltName.size()) {
				continue;
			}
			Object type = subjectAltName.get(0);
			Object value = subjectAltName.get(1);
			if (value instanceof String sanValue && type instanceof Integer typeValue
					&& (DNS_NAME_TYPE == typeValue || IP_ADDRESS_TYPE == typeValue)
					&& matchesHostName(sanValue, hostname)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the given hostname matches the certificate's common name, false otherwise.
	 *
	 * @param hostname the hostname to verify
	 * @param certificate the certificate to check
	 * @return true if the hostname matches the common name, false otherwise
	 */
	static boolean matchesCommonName(final String hostname, final X509Certificate certificate) {
		String subjectDn = certificate.getSubjectX500Principal().getName();
		for (String dnPart : subjectDn.split(",")) {
			String trimmed = dnPart.trim();
			if (trimmed.regionMatches(true, 0, "CN=", 0, 3)) {
				String commonName = trimmed.substring(3);
				return matchesHostName(commonName, hostname);
			}
		}
		return false;
	}

	/**
	 * Returns true if the given certificate name matches the requested hostname, false otherwise. Supports the common
	 * wildcard form <code>*.&lt;domain&gt;</code>.
	 *
	 * @param certificateName name from the certificate, may contain a wildcard
	 * @param hostname the requested hostname
	 * @return true if the names match, false otherwise
	 */
	static boolean matchesHostName(final String certificateName, final String hostname) {
		if (certificateName.startsWith("*.")) {
			String wildcardDomain = certificateName.substring(1);
			return hostname.regionMatches(true, 0, wildcardDomain, 0, wildcardDomain.length()) // NOSONAR
					|| hostname.endsWith(wildcardDomain) && hostname.length() > wildcardDomain.length();
		}
		return certificateName.equalsIgnoreCase(hostname);
	}

	/**
	 * Returns the trust managers for the given truststore configuration, rethrowing any security failure as an
	 * {@link IllegalStateException}.
	 *
	 * @param trustStoreInfo truststore configuration
	 * @return the trust managers for the given configuration, may be null
	 */
	static TrustManager[] trustManagers(final StoreInfo trustStoreInfo) {
		try {
			return SSLContexts.getTrustManagers(trustStoreInfo);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Failed to create trust managers from the truststore configuration: " + e.getMessage(), e);
		}
	}

	/**
	 * Returns the first {@link X509TrustManager} from the given trust managers, or the platform default trust manager if
	 * none could be created from the given array.
	 *
	 * @param trustManagers trust managers to inspect, may be null
	 * @return an X509 trust manager
	 */
	static X509TrustManager x509TrustManager(final TrustManager[] trustManagers) {
		if (null != trustManagers) {
			for (TrustManager trustManager : trustManagers) {
				if (trustManager instanceof X509TrustManager x509TrustManager) {
					return x509TrustManager;
				}
			}
		}
		return defaultTrustManager();
	}

	/**
	 * Returns the platform default {@link X509TrustManager}.
	 *
	 * @return the platform default X509 trust manager
	 */
	static X509TrustManager defaultTrustManager() {
		return ThrowingSupplier.unchecked(() -> {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
				if (trustManager instanceof X509TrustManager x509TrustManager) {
					return x509TrustManager;
				}
			}
			throw new GeneralSecurityException("No X509TrustManager found in default trust managers");
		}).get();
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
