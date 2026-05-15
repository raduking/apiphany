package org.apiphany.http;

import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;

/**
 * Utility class for Java net HTTP library related operations.
 * <p>
 * This class provides information about the presence of the Java net HTTP library in the classpath and should not have
 * any Java net HTTP-specific dependencies itself.
 *
 * @author Radu Sebastian LAZIN
 */
public class JavaNetHttpLibrary {

	/**
	 * The client library name for Java net HTTP.
	 */
	public static final String CLIENT_NAME = "java-net-http";

	/**
	 * The Java net HTTP HttpClient class name.
	 */
	private static final String JAVA_NET_HTTP_HTTP_CLIENT_CLASS_NAME = "java.net.http.HttpClient";

	/**
	 * Checks if the Java net HTTP library is present in the classpath.
	 *
	 * @return {@code true} if the Java net HTTP library is present, {@code false} otherwise
	 */
	public static boolean isPresent() {
		return Reflection.isClassPresent(JAVA_NET_HTTP_HTTP_CLIENT_CLASS_NAME);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private JavaNetHttpLibrary() {
		throw Constructors.unsupportedOperationException();
	}
}
