package org.apiphany.security.ssl.client;

import javax.net.ssl.SSLContext;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.DecoratingExchangeClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.logging.Slf4jLoggerAdapter;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.SSLContextAware;
import org.apiphany.security.ssl.SSLProperties;
import org.morphix.lang.Messages;
import org.morphix.lang.Nullables;
import org.morphix.lang.resource.ScopedResource;

/**
 * Decorates an {@link ExchangeClient} with SSL/TLS authentication type semantics.
 * <p>
 * The actual SSL configuration (SSLContext, keystore, truststore) is configured on the underlying client through
 * {@link SSLProperties} in the client properties. This decorator primarily:
 * <ul>
 * <li>marks the authentication type as {@link AuthenticationType#SSL}</li>
 * <li>exposes the {@link SSLProperties} and {@link SSLContext} for inspection and diagnostics via
 * {@link #getSslProperties()} and {@link #getSslContext()} respectively</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class SSLHttpExchangeClient extends DecoratingExchangeClient implements HttpExchangeClient {

	/**
	 * Class logger.
	 */
	private static final Slf4jLoggerAdapter LOGGER = Slf4jLoggerAdapter.of(SSLHttpExchangeClient.class);

	/**
	 * Decorates an exchange client with SSL authentication.
	 *
	 * @param delegate the underlying exchange client (must be SSL-configured)
	 */
	public SSLHttpExchangeClient(final ScopedResource<ExchangeClient> delegate) {
		super(delegate);
		try {
			validateDelegate(delegate);
		} finally {
			// If validation fails, we need to close the delegate if it's managed to avoid resource leaks
			closeIfManaged(delegate);
		}
	}

	/**
	 * Decorates an exchange client with SSL authentication. The delegate is considered unmanaged.
	 *
	 * @param delegate the underlying exchange client (must be SSL-configured)
	 */
	@SuppressWarnings("resource")
	public SSLHttpExchangeClient(final ExchangeClient delegate) {
		this(ScopedResource.unmanaged(delegate));
	}

	/**
	 * Validates that the delegate exchange client is SSL-configured and implements SSLContext awareness.
	 *
	 * @param delegate the underlying exchange client to validate
	 * @throws IllegalStateException if the delegate is not SSL-configured or does not implement SSLContext awareness
	 */
	@SuppressWarnings("resource")
	protected void validateDelegate(final ScopedResource<ExchangeClient> delegate) {
		ExchangeClient client = delegate.unwrap();
		if (!(client instanceof SSLContextAware sslContextAware)) {
			throw new IllegalStateException(Messages.message("Underlying exchange client: {}, must be SSL-configured and must implement: {}",
					client.getClass(), SSLContextAware.class));
		}
		if (null == sslContextAware.getSslContext()) {
			throw new IllegalStateException(Messages.message("Underlying exchange client: {}, must have a non-null SSL context", client.getClass()));
		}
	}

	/**
	 * Closes the underlying exchange client if it is managed. If an exception occurs during closing, it is logged but not
	 * rethrown to avoid masking the original exception that caused the constructor to fail.
	 *
	 * @param delegate the underlying exchange client to close if managed
	 */
	@SuppressWarnings("resource")
	protected void closeIfManaged(final ScopedResource<ExchangeClient> delegate) {
		delegate.closeIfManaged(e -> LOGGER.error("Error closing unmanaged exchange client: {}", delegate.unwrap().getClass(), e));
	}

	/**
	 * @see ExchangeClient#getAuthenticationType()
	 */
	@Override
	public AuthenticationType getAuthenticationType() {
		return AuthenticationType.SSL;
	}

	/**
	 * Returns the SSL properties.
	 *
	 * @return the SSL properties
	 */
	public SSLProperties getSslProperties() {
		return Nullables.<ClientProperties, SSLProperties>whenNotNull(getClientProperties(),
				props -> props.getCustomProperties(SSLProperties.class));
	}

	/**
	 * Returns the SSL context.
	 *
	 * @return the SSL context
	 */
	@Override
	public SSLContext getSslContext() {
		SSLContextAware sslContextAware = (SSLContextAware) getExchangeClient();
		return sslContextAware.getSslContext();
	}
}
