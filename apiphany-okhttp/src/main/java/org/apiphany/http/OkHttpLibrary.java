package org.apiphany.http;

import org.morphix.reflection.Classes;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Reflection;
import org.morphix.runtime.LibraryVersion;

/**
 * Utility class for OkHttp library related operations.
 * <p>
 * This class provides information about the presence of the OkHttp library in the classpath and should not have any
 * OkHttp-specific dependencies itself.
 *
 * @author Radu Sebastian LAZIN
 */
public class OkHttpLibrary {

	/**
	 * The client library name for OkHttp.
	 */
	public static final String CLIENT_NAME = "okhttp";

	/**
	 * The minimum OkHttp version this module was built against and requires at runtime: <code>4.9.0</code>.
	 */
	private static final LibraryVersion MINIMUM_VERSION = LibraryVersion.of(CLIENT_NAME, 4, 9, 0);

	/**
	 * The maximum OkHttp version this module supports, i.e. the highest version it was tested against: <code>4.12.1</code>.
	 * <p>
	 * A newer runtime version is outside the tested range and is rejected by {@link #verifyVersion()}.
	 */
	private static final LibraryVersion MAXIMUM_VERSION = LibraryVersion.of(CLIENT_NAME, 4, 12, 1);

	/**
	 * The OkHttp OkHttpClient class name.
	 */
	private static final String OKHTTP_CLIENT_CLASS_NAME = "okhttp3.OkHttpClient";

	/**
	 * The OkHttp {@code Platform} class name, used as the version anchor because the OkHttp jar does not carry an
	 * {@code Implementation-Version} manifest entry.
	 */
	private static final String OKHTTP_PLATFORM_CLASS_NAME = "okhttp3.internal.platform.Platform";

	/**
	 * The runtime version of the OkHttp library. OkHttp jars do not declare an {@code Implementation-Version} manifest
	 * entry, so the version cannot be detected at runtime and this is always {@code null}; use {@link #isPresent()} to
	 * check for the library instead.
	 */
	private static final LibraryVersion VERSION = LibraryVersion.of(CLIENT_NAME, Classes.Safe.getOne(OKHTTP_PLATFORM_CLASS_NAME));

	/**
	 * Checks if the OkHttp library is present in the classpath.
	 *
	 * @return {@code true} if the OkHttp library is present, {@code false} otherwise
	 */
	public static boolean isPresent() {
		return Reflection.isClassPresent(OKHTTP_CLIENT_CLASS_NAME);
	}

	/**
	 * Returns the version of the OkHttp library found on the classpath at runtime, read from the
	 * {@code Implementation-Version} manifest entry of the jar containing {@code OkHttpClient}, or {@code null} if the
	 * library is not present or the version cannot be determined (e.g. running from an IDE output directory instead of a
	 * jar).
	 *
	 * @return the runtime OkHttp version, or {@code null} if it cannot be determined
	 */
	public static LibraryVersion version() {
		return VERSION;
	}

	/**
	 * Returns the minimum OkHttp version this module was built against and requires at runtime.
	 *
	 * @return the minimum required OkHttp version
	 */
	public static LibraryVersion minimumVersion() {
		return MINIMUM_VERSION;
	}

	/**
	 * Returns the maximum OkHttp version this module was built against and requires at runtime.
	 *
	 * @return the maximum required OkHttp version
	 */
	public static LibraryVersion maximumVersion() {
		return MAXIMUM_VERSION;
	}

	/**
	 * Verifies that the OkHttp library found on the classpath at runtime is within the supported range, i.e. at least
	 * {@link #MINIMUM_VERSION} and at most {@link #MAXIMUM_VERSION}. Versions outside that range are rejected: older
	 * versions are missing APIs this module relies on, while newer versions are outside the tested range.
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
	private OkHttpLibrary() {
		throw Constructors.unsupportedOperationException();
	}
}
