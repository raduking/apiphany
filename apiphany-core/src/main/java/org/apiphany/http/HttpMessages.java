package org.apiphany.http;

import java.net.http.HttpClient.Version;
import java.util.Locale;
import java.util.function.Predicate;

import org.apiphany.lang.Require;
import org.morphix.lang.Nullables;
import org.morphix.lang.Throwables;
import org.morphix.reflection.Constructors;

/**
 * Utility methods for HTTP requests/responses.
 *
 * @author Radu Sebastian LAZIN
 */
public class HttpMessages {

	/**
	 * Carriage Return Line Feed, used in HTTP protocol to separate lines.
	 */
	public static final String CRLF = "\r\n";

	/**
	 * Parses the HTTP version string.
	 *
	 * @param versionString HTTP version string
	 * @return a {@link Version} object
	 */
	public static Version parseJavaNetHttpVersion(final String versionString) {
		return switch (versionString) {
			case "HTTP/1.1" -> Version.HTTP_1_1;
			case "HTTP/2" -> Version.HTTP_2;
			default -> throw new IllegalArgumentException("Unsupported HTTP version: " + versionString);
		};
	}

	/**
	 * Transforms a {@link Version} object into it's HTTP protocol string
	 *
	 * @param version HTTP version
	 * @return a {@link Version} object
	 */
	public static String toProtocolString(final Version version) {
		return switch (version) {
			case HTTP_1_1 -> "HTTP/1.1";
			case HTTP_2 -> "HTTP/2";
		};
	}

	/**
	 * Returns the range string for the RANGE HTTP header.
	 *
	 * @param rangeStart start of the range
	 * @param rangeEnd end of the range
	 * @return range string
	 * @throws IllegalArgumentException if rangeEnd is less than rangeStart
	 */
	public static String getRangeString(final Long rangeStart, final Long rangeEnd) throws IllegalArgumentException {
		long actualRangeStart = Nullables.nonNullOrDefault(rangeStart, () -> 0L);
		long actualRangeEnd = Nullables.nonNullOrDefault(rangeEnd, () -> 0L);
		Require.that(actualRangeStart <= actualRangeEnd, "rangeEnd must be greater or equal to rangeStart");
		return "bytes=" + actualRangeStart + "-" + actualRangeEnd;
	}

	/**
	 * Returns the default redirect-loop failure predicate based on throwable message.
	 *
	 * @return default redirect-loop failure predicate
	 */
	public static Predicate<Throwable> defaultRedirectLoopFailurePredicate() {
		return t -> null != t && isRedirectLoopFailureMessage(t.getMessage());
	}

	/**
	 * Returns true when the throwable indicates redirect-loop/too-many-redirects failure.
	 *
	 * @param throwable throwable to inspect
	 * @return true if redirect loop was detected, false otherwise
	 */
	public static boolean isRedirectLoopFailure(final Throwable throwable) {
		return isRedirectLoopFailure(throwable, defaultRedirectLoopFailurePredicate());
	}

	/**
	 * Returns true when the throwable indicates redirect-loop/too-many-redirects failure.
	 *
	 * @param throwable throwable to inspect
	 * @param redirectLoopFailurePredicate predicate to determine if a throwable indicates redirect-loop/too-many-redirects
	 *     failure
	 * @return true if redirect loop was detected, false otherwise
	 */
	public static boolean isRedirectLoopFailure(final Throwable throwable, final Predicate<Throwable> redirectLoopFailurePredicate) {
		return Throwables.anyMatch(throwable, redirectLoopFailurePredicate);
	}

	/**
	 * Returns true when the message indicates redirect-loop/too-many-redirects failure.
	 *
	 * @param message message to inspect
	 * @return true if redirect loop was detected, false otherwise
	 */
	private static boolean isRedirectLoopFailureMessage(final String message) {
		if (null == message) {
			return false;
		}
		String lowerCaseMessage = message.toLowerCase(Locale.ROOT);
		return lowerCaseMessage.contains("circular redirect")
				|| lowerCaseMessage.contains("too many redirects");
	}

	/**
	 * Hide constructor.
	 */
	private HttpMessages() {
		throw Constructors.unsupportedOperationException();
	}
}
