package org.apiphany.http;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ClientProperties.Timeout;
import org.apiphany.client.http.JavaNetHttpProperties;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.Suppliers;

/**
 * Utility interface for Java Net HTTP library related operations.
 *
 * @author Radu Sebastian LAZIN
 */
public interface JavaNetHttpClients {

	/**
	 * Customizes the HTTP client builder based on the given client properties and SSL context.
	 *
	 * @param httpClientBuilder HTTP client builder
	 * @param clientProperties client properties
	 * @param sslContext SSL context to set in the HTTP client builder
	 * @return the customized HTTP client builder
	 */
	static HttpClient.Builder customize(final HttpClient.Builder httpClientBuilder, final ClientProperties clientProperties,
			final SSLContext sslContext) {
		JavaNetHttpProperties httpProperties = clientProperties.getCustomProperties(JavaNetHttpProperties.class);

		// HTTP version
		HttpClient.Version version = Nullables.notNull(httpProperties)
				.andNotNull(JavaNetHttpProperties::getRequest)
				.thenNotNull(JavaNetHttpProperties.Request::getHttpVersion)
				.orElse(() -> JavaNetHttpProperties.Request.Default.HTTP_VERSION);
		httpClientBuilder.version(version);

		// SSL context
		Nullables.whenNotNull(sslContext, httpClientBuilder::sslContext);

		// Timeouts
		Duration connectTimeout = getTimeout(clientProperties.getTimeout(), Timeout::getConnect);
		Nullables.whenNotNull(connectTimeout, httpClientBuilder::connectTimeout);

		// Follow redirects
		boolean followRedirects = clientProperties.getConnection().isFollowRedirects();
		httpClientBuilder.followRedirects(followRedirects ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER);
		return httpClientBuilder;
	}

	/**
	 * Returns the usable timeout value based on the given timeout and timeout extractor. If the timeout value is equal to
	 * {@link ClientProperties.Timeout#INFINITE}, then this method will return null to indicate that no timeout should be
	 * applied.
	 * <p>
	 * This method is only useful for the Java net HTTP client because whenever a zero (infinite) timeout is set it throws
	 * an exception instead of just treating it as infinite.
	 *
	 * @param timeout timeout object containing the timeout value
	 * @param timeoutExtractor function to extract the timeout value from the timeout object
	 * @return the usable timeout value or null if no timeout should be applied
	 */
	static Duration getTimeout(final Timeout timeout, final Function<Timeout, Duration> timeoutExtractor) {
		return getTimeout(timeout, timeoutExtractor, Suppliers.supplyNull());
	}

	/**
	 * Returns the usable timeout value based on the given timeout and timeout extractor. If the timeout value is
	 * {@code null} equal to {@link ClientProperties.Timeout#INFINITE}, then this method will return the default value
	 * provided by the default value supplier.
	 * <p>
	 * This method is only useful for the Java net HTTP client because whenever a zero (infinite) timeout is set it throws
	 * an exception instead of just treating it as infinite.
	 *
	 * @param timeout timeout object containing the timeout value
	 * @param timeoutExtractor function to extract the timeout value from the timeout object
	 * @param defaultValueSupplier supplier for the default timeout value to return if the extracted timeout value is null
	 *     or infinite
	 * @return the usable timeout value or the default value if the extracted timeout value is infinite
	 */
	static Duration getTimeout(final Timeout timeout, final Function<ClientProperties.Timeout, Duration> timeoutExtractor,
			final Supplier<Duration> defaultValueSupplier) {
		Duration timeoutValue = timeoutExtractor.apply(timeout);
		if (null == timeoutValue) {
			return defaultValueSupplier.get();
		}
		if (Objects.equals(timeoutValue, ClientProperties.Timeout.INFINITE)) {
			return defaultValueSupplier.get();
		}
		return timeoutValue;
	}
}
