package org.apiphany.header;

import java.util.Collections;
import java.util.List;

import org.apiphany.lang.Strings;

/**
 * Represents a node in a chain of responsibility for resolving HTTP header values. Each instance can either handle a
 * header request directly or delegate it to the next {@link HeaderValues} in the chain. By default, this implementation
 * returns an empty list for all header requests, serving as a base class that can be extended for custom behavior.
 * <p>
 * This class is typically used in conjunction with {@link HeaderValuesChain} to form a linked structure of header value
 * resolvers.
 *
 * @author Radu Sebastian LAZIN
 */
public class HeaderValues {

	/**
	 * The string used to redact sensitive information.
	 */
	public static final String REDACTED = "REDACTED";

	/**
	 * The next {@link HeaderValues} in the chain of responsibility. If {@code null}, this represents the end of the chain.
	 */
	private HeaderValues next;

	/**
	 * Default constructor.
	 */
	protected HeaderValues() {
		// empty
	}

	/**
	 * Retrieves values for the specified header. The default implementation always returns an empty list. Subclasses should
	 * override this method to provide specific header resolution logic.
	 *
	 * @param <N> header name type
	 * @param header the name of the header to retrieve (case sensitivity depends on implementation)
	 * @param headers the context object containing header information (type may vary by implementation)
	 * @return an empty list by default, implementations should return specific header values
	 */
	public <N> List<String> get(final N header, final Object headers) {
		return Collections.emptyList();
	}

	/**
	 * Returns true if the given headers contain the given header with the given value, false otherwise.
	 *
	 * @param <N> header name type
	 * @param <V> header value type
	 * @param headerName header name
	 * @param headerValue header value
	 * @param headers headers to check
	 * @return true if the given headers contain the given header with the given value, false otherwise
	 */
	public <N, V> boolean contains(final N headerName, final V headerValue, final Object headers) {
		return Headers.contains(headerName, headerValue, name -> get(name, headers));
	}

	/**
	 * Gets the next {@link HeaderValues} in the chain of responsibility.
	 *
	 * @return the next {@link HeaderValues} in the chain, or {@code null} if this is the end
	 * @throws IllegalStateException if called on the last element in the chain
	 */
	public HeaderValues getNext() {
		if (null == next) {
			throw new IllegalStateException("Cannot get header values, end of the chain reached.");
		}
		return next;
	}

	/**
	 * Sets the next {@link HeaderValues} in the chain of responsibility.
	 *
	 * @param next the {@link HeaderValues} to set as next in the chain, or {@code null} to indicate the end of the chain
	 */
	public void setNext(final HeaderValues next) {
		this.next = next;
	}

	/**
	 * Builds the value for the header for the request by concatenating the two parameters with whitespace between them.
	 *
	 * @param <L> left value type
	 * @param <R> right value type
	 *
	 * @param lvalue left value
	 * @param rvalue right value
	 * @return the header value
	 */
	public static <L, R> String value(final L lvalue, final R rvalue) {
		return String.join(" ", Strings.safeToString(lvalue), Strings.safeToString(rvalue));
	}
}
