package org.apiphany.security.tls;

/**
 * Interface representing a TLS key exchange mechanism.
 * <p>
 * This interface serves as a marker for different TLS key exchange algorithms and extends {@link TLSObject} to provide
 * common TLS-related functionality. Implementations of this interface should provide specific key exchange mechanisms
 * used during TLS handshake.
 * </p>
 * <p>
 * The interface is intentionally left empty as it serves primarily as a type identifier, with actual key exchange
 * implementations providing their specific methods.
 * </p>
 *
 * @see TLSObject
 *
 * @author Radu Sebastian LAZIN
 */
public interface TLSKeyExchange extends TLSObject {

	// empty

}
