package org.apiphany.security.ssl;

import java.security.Provider;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;

import org.morphix.reflection.Fields;

public class SSLContextAdapter extends SSLContext {

	private final SSLContextSpi sslContextSpi;

	public SSLContextAdapter(final SSLContext sslContext) {
		this(Fields.IgnoreAccess.get(sslContext, "contextSpi"), sslContext.getProvider(), sslContext.getProtocol());
	}

	protected SSLContextAdapter(final SSLContextSpi contextSpi, final Provider provider, final String protocol) {
		super(contextSpi, provider, protocol);
		this.sslContextSpi = contextSpi;
	}

	public void setSecureRandom(final SecureRandom secureRandom) {
		Fields.IgnoreAccess.set(sslContextSpi, "secureRandom", secureRandom);
	}

}
