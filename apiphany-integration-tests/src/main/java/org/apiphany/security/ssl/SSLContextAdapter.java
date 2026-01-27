package org.apiphany.security.ssl;

import java.security.Provider;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;

import org.morphix.reflection.Fields;

/**
 * Adapter class for {@link SSLContext} to allow modification of internal fields.
 *
 * @author Radu Sebastian LAZIN
 */
public class SSLContextAdapter extends SSLContext {

	/**
	 * Underlying {@link SSLContextSpi} instance.
	 */
	private final SSLContextSpi sslContextSpi;

	/**
	 * Constructor initializing the adapter with the given {@link SSLContext}.
	 *
	 * @param sslContext the SSLContext to adapt
	 */
	public SSLContextAdapter(final SSLContext sslContext) {
		this(Fields.IgnoreAccess.get(sslContext, "contextSpi"), sslContext.getProvider(), sslContext.getProtocol());
	}

	/**
	 * Protected constructor initializing the adapter with the given SSLContextSpi, Provider, and protocol.
	 *
	 * @param contextSpi the SSLContextSpi instance
	 * @param provider the security provider
	 * @param protocol the protocol name
	 */
	protected SSLContextAdapter(final SSLContextSpi contextSpi, final Provider provider, final String protocol) {
		super(contextSpi, provider, protocol);
		this.sslContextSpi = contextSpi;
	}

	/**
	 * Sets the SecureRandom instance used by the {@link SSLContext}.
	 *
	 * @param secureRandom the SecureRandom instance to set
	 */
	public void setSecureRandom(final SecureRandom secureRandom) {
		Fields.IgnoreAccess.set(sslContextSpi, "secureRandom", secureRandom);
	}
}
