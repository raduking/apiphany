package org.apiphany.security.tls;

/**
 * Represents the body content of a TLS handshake message.
 * <p>
 * This interface extends {@link TLSObject} to provide additional handshake-specific functionality. Implementations of
 * this interface:
 * <ul>
 * <li>Must define their specific handshake type</li>
 * <li>Maintain all properties of a {@link TLSObject} (binary representation and known size)</li>
 * <li>Represent the variable-length payload portion of a TLS handshake message</li>
 * </ul>
 *
 * <p>
 * The handshake body structure varies depending on the {@link HandshakeType}, with each type having its own specific
 * format and requirements.
 *
 * @see TLSObject
 * @see HandshakeType
 *
 * @author Radu Sebastian LAZIN
 */
public interface TLSHandshakeBody extends TLSObject {

	/**
	 * Returns the type of this handshake message body.
	 *
	 * @return The {@link HandshakeType} that identifies the structure and purpose of this handshake message body. Will
	 * never be {@code null}.
	 */
	HandshakeType getType();

}
