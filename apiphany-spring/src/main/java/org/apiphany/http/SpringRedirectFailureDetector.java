package org.apiphany.http;

import org.morphix.reflection.Constructors;

/**
 * Utility class for detecting redirect failures in Spring-backed clients.
 *
 * @author Radu Sebastian LAZIN
 */
public class SpringRedirectFailureDetector {

	/**
	 * Returns true if the throwable indicates a redirect failure for Spring-backed clients.
	 *
	 * @param throwable throwable to inspect
	 * @return true if redirect failure was detected, false otherwise
	 */
	public static boolean isRedirectFailure(final Throwable throwable) {
		return ApacheHC5Library.isPresent() && ApacheHC5Clients.isCircularRedirectException(throwable);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private SpringRedirectFailureDetector() {
		throw Constructors.unsupportedOperationException();
	}
}
