package org.apiphany.header;

import java.util.Collections;
import java.util.List;

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
	 * The next {@link HeaderValues} in the chain of responsibility. If {@code null}, this represents the end of the chain.
	 */
	private HeaderValues next;

	/**
	 * Retrieves values for the specified header. The default implementation always returns an empty list. Subclasses should
	 * override this method to provide specific header resolution logic.
	 *
	 * @param header the name of the header to retrieve (case sensitivity depends on implementation)
	 * @param headers the context object containing header information (type may vary by implementation)
	 * @return an empty list by default, implementations should return specific header values
	 */
	public List<String> get(final String header, final Object headers) {
		return Collections.emptyList();
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
}
