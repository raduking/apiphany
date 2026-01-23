package org.apiphany.security.tls;

import org.apiphany.security.tls.ext.ExtensionType;

/**
 * Represents a TLS extension that can be included in handshake messages.
 * <p>
 * Extensions provide a way to expand the functionality of the TLS protocol while maintaining backward compatibility.
 * Each extension has a specific type and format defined in various TLS RFCs.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.4.1.4">RFC 5246 - Extensions</a>
 * @see <a href="https://www.iana.org/assignments/tls-extensiontype-values/tls-extensiontype-values.xhtml"> IANA TLS
 * ExtensionType Values Registry</a>
 *
 * @author Radu Sebastian LAZIN
 */
public interface TLSExtension extends TLSObject {

	/**
	 * Returns the type of this TLS extension.
	 *
	 * @return the extension type enum value that identifies this extension's purpose and format
	 */
	ExtensionType getType();

}
