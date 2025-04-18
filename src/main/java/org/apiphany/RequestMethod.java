package org.apiphany;

/**
 * Represents a request method (verb) used in network communication, such as GET or POST. Defines the contract for
 * methods that identify types of operations in a client-server interaction.
 *
 * <p>
 * Common use cases include:
 * <ul>
 * <li>Defining API endpoint operations</li>
 * <li>Routing or validating incoming requests</li>
 * <li>Generating outgoing requests in a client</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public interface RequestMethod {

	/**
	 * Returns the canonical name of the method (e.g., "GET" or "POST"). The name should be consistent with standard
	 * conventions.
	 *
	 * @return The formal name of the method, never {@code null} or empty.
	 */
	String name();

	/**
	 * Returns the normalized value of the method. This may differ from {@link #name()} in cases where case sensitivity or
	 * aliases are supported (e.g., "get" vs. "GET").
	 *
	 * @return The method's value as used in requests, never {@code null}.
	 */
	String value();
}
