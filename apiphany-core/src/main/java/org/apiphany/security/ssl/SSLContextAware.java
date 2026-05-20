package org.apiphany.security.ssl;

import javax.net.ssl.SSLContext;

/**
 * Interface for components that are aware of the SSL context used for secure communication.
 *
 * @author Radu Sebastian LAZIN
 */
public interface SSLContextAware {

	/**
	 * Returns the SSL context used for secure communication, or {@code null} if not available.
	 *
	 * @return the SSL context, or null if not available
	 */
	default SSLContext getSslContext() {
		return null;
	}
}
