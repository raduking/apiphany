package org.apiphany.security.ssl.client;

import javax.net.ssl.SSLContext;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.DecoratingExchangeClient;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.SSLContextAware;
import org.apiphany.security.ssl.SSLProperties;
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
	 * Decorates an exchange client with SSL authentication.
	 *
	 * @param delegate the underlying exchange client (must be SSL-configured)
	 */
	public SSLHttpExchangeClient(final ScopedResource<ExchangeClient> delegate) {
		super(delegate);
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
	@SuppressWarnings("resource")
	public SSLContext getSslContext() {
		if (getExchangeClient() instanceof SSLContextAware sslContextAware) {
			return sslContextAware.getSslContext();
		}
		return HttpExchangeClient.super.getSslContext();
	}
}
