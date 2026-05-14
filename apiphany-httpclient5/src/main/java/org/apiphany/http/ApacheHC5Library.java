package org.apiphany.http;

import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;

/**
 * Utility class for Apache HttpClient 5 library related operations.
 * <p>
 * This class provides information about the presence of the Apache HttpClient 5 library in the classpath and should not
 * have any Apache HttpClient 5-specific dependencies itself.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApacheHC5Library {

	/**
	 * The client library name for Apache HttpClient 5.
	 */
	public static final String CLIENT_NAME = "http-client5";

	/**
	 * The Apache HttpClient 5 CloseableHttpClient class name.
	 */
	private static final String APACHE_HC5_CLOSEABLE_HTTP_CLIENT_CLASS_NAME =
			"org.apache.hc.client5.http.impl.classic.CloseableHttpClient";

	/**
	 * Checks if the Apache HttpClient 5 library is present in the classpath.
	 *
	 * @return {@code true} if the Apache HttpClient 5 library is present, {@code false} otherwise
	 */
	public static boolean isPresent() {
		return Reflection.isClassPresent(APACHE_HC5_CLOSEABLE_HTTP_CLIENT_CLASS_NAME);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ApacheHC5Library() {
		throw Constructors.unsupportedOperationException();
	}
}
