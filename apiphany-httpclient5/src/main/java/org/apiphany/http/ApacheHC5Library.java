package org.apiphany.http;

import org.morphix.reflection.Classes;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;
import org.morphix.runtime.LibraryVersion;

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
	 * The minimum Apache HttpClient 5 version this module was built against and requires at runtime: <code>5.5.0</code>.
	 */
	private static final LibraryVersion MINIMUM_VERSION = LibraryVersion.of(CLIENT_NAME, 5, 5);

	/**
	 * The maximum Apache HttpClient 5 version this module supports, i.e. the highest version it was tested against:
	 * <code>5.6.4</code>.
	 * <p>
	 * A newer runtime version is outside the tested range and is rejected by {@link #verifyVersion()}.
	 */
	private static final LibraryVersion MAXIMUM_VERSION = LibraryVersion.of(CLIENT_NAME, 5, 6, 4);

	/**
	 * The Apache HttpClient 5 CloseableHttpClient class name.
	 */
	private static final String CLOSEABLE_HTTP_CLIENT_CLASS_NAME =
			"org.apache.hc.client5.http.impl.classic.CloseableHttpClient";

	/**
	 * The runtime version of the Apache HttpClient 5 library, as detected from {@link #CLOSEABLE_HTTP_CLIENT_CLASS_NAME}.
	 */
	private static final LibraryVersion VERSION = LibraryVersion.of(CLIENT_NAME, Classes.Safe.getOne(CLOSEABLE_HTTP_CLIENT_CLASS_NAME));

	/**
	 * Checks if the Apache HttpClient 5 library is present in the classpath.
	 *
	 * @return {@code true} if the Apache HttpClient 5 library is present, {@code false} otherwise
	 */
	public static boolean isPresent() {
		return Reflection.isClassPresent(CLOSEABLE_HTTP_CLIENT_CLASS_NAME);
	}

	/**
	 * Returns the version of the Apache HttpClient 5 library found on the classpath at runtime, read from the
	 * {@code Implementation-Version} manifest entry of the jar containing {@code CloseableHttpClient}, or {@code null} if
	 * the library is not present or the version cannot be determined (e.g. running from an IDE output directory instead of
	 * a jar).
	 *
	 * @return the runtime Apache HttpClient 5 version, or {@code null} if it cannot be determined
	 */
	public static LibraryVersion version() {
		return VERSION;
	}

	/**
	 * Returns the minimum Apache HttpClient 5 version this module was built against and requires at runtime.
	 *
	 * @return the minimum required Apache HttpClient 5 version
	 */
	public static LibraryVersion minimumVersion() {
		return MINIMUM_VERSION;
	}

	/**
	 * Returns the maximum Apache HttpClient 5 version this module was built against and requires at runtime.
	 *
	 * @return the maximum required Apache HttpClient 5 version
	 */
	public static LibraryVersion maximumVersion() {
		return MAXIMUM_VERSION;
	}

	/**
	 * Verifies that the Apache HttpClient 5 library found on the classpath at runtime is within the supported range, i.e.
	 * at least {@link #MINIMUM_VERSION} and at most {@link #MAXIMUM_VERSION}. Versions outside that range are rejected:
	 * older versions are missing APIs this module relies on, while newer versions are outside the tested range.
	 * <p>
	 * If the runtime version cannot be determined (e.g. missing manifest information) the check is skipped since it cannot
	 * be reliably enforced.
	 *
	 * @throws IllegalStateException if a runtime version older than {@link #MINIMUM_VERSION} or newer than
	 *     {@link #MAXIMUM_VERSION} is detected
	 */
	public static void verifyVersion() {
		VERSION.verifyAtLeast(MINIMUM_VERSION);
		VERSION.verifyAtMost(MAXIMUM_VERSION);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ApacheHC5Library() {
		throw Constructors.unsupportedOperationException();
	}
}
