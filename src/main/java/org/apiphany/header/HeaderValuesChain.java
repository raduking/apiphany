package org.apiphany.header;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.morphix.lang.Nullables;

/**
 * A chain of {@link HeaderValues} objects that allows for layered header value resolution. The chain maintains an
 * ordered list of {@link HeaderValues} where the most recently added instance is first in the list and becomes the
 * primary source for header resolution.
 * <p>
 * This class follows the Chain of Responsibility pattern, where each {@link HeaderValues} in the chain can either
 * handle a header request or delegate it to the next in the chain.
 *
 * @author Radu Sebastian LAZIN
 */
public class HeaderValuesChain {

	/**
	 * The list of {@link HeaderValues} instances in this chain, maintained in insertion order with the most recently added
	 * instance at the beginning.
	 */
	private List<HeaderValues> headerValuesList = new LinkedList<>();

	/**
	 * Constructs an empty {@code HeaderValuesChain} initialized with one {@link HeaderValues} instance as the default
	 * handler.
	 */
	public HeaderValuesChain() {
		headerValuesList.add(new HeaderValues());
	}

	/**
	 * Adds a new {@link HeaderValues} instance to the beginning of the chain. The added instance will be the first one
	 * consulted for header values.
	 *
	 * @param headerValues the {@link HeaderValues} instance to add to the chain
	 * @throws NullPointerException if {@code headerValues} is null
	 */
	public void add(final HeaderValues headerValues) {
		Objects.requireNonNull(headerValues).setNext(headerValuesList.getFirst());
		headerValuesList.addFirst(headerValues);
	}

	/**
	 * Retrieves the values for the specified header from the chain of {@link HeaderValues}. The chain is traversed in order
	 * until the first non-null result is found.
	 *
	 * @param header the name of the header to retrieve
	 * @param headers the context object containing header information (type may vary by implementation)
	 * @return a list of values for the specified header, or {@code null} if no values were found
	 */
	public List<String> get(final String header, final Object headers) {
		return Nullables.apply(headerValuesList.getFirst(), hv -> hv.get(header, headers));
	}
}
