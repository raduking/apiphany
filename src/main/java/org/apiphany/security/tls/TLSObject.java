package org.apiphany.security.tls;

import org.apiphany.io.BinaryRepresentable;
import org.apiphany.io.ByteSizeable;

/**
 * Marker interface for all objects that participate in TLS protocol communication.
 * <p>
 * Implementing this interface indicates that the object:
 * <ul>
 * <li>Has a well-defined binary representation for use in TLS messages (via {@link BinaryRepresentable})</li>
 * <li>Has a known size when serialized (via {@link ByteSizeable})</li>
 * </ul>
 *
 * @see BinaryRepresentable
 * @see ByteSizeable
 *
 * @author Radu Sebastian LAZIN
 */
public interface TLSObject extends ByteSizeable, BinaryRepresentable {

	// empty - serves as a type marker and composition of two interfaces

}
