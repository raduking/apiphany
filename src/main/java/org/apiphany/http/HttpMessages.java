package org.apiphany.http;

import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import org.morphix.lang.Nullables;
import org.morphix.reflection.Constructors;

/**
 * Utility methods for HTTP requests/responses.
 *
 * @author Radu Sebastian LAZIN
 */
public class HttpMessages {

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
		if (actualRangeEnd < actualRangeStart) {
			throw new IllegalArgumentException("rangeEnd must be greater or equal to rangeStart");
		}
		return String.format("bytes=%d-%d", actualRangeStart, actualRangeEnd);
	}

	/**
	 * Retrieves the values of a specific header from the provided {@link HttpHeaders} object.
	 *
	 * @param header the name of the header whose values are to be retrieved.
	 * @param headers the {@link HttpHeaders} object from which to retrieve the header values.
	 * @return a list of values for the specified header. If the header is not found, an empty list is returned.
	 */
	public static List<String> getHeaderValues(final String header, final HttpHeaders headers) {
		return headers.allValues(header);
	}

	/**
	 * Retrieves the values of a specific header from the provided {@link Map} of headers. The map is converted to an
	 * {@link HttpHeaders} object internally to fetch the header values.
	 *
	 * @param header the name of the header whose values are to be retrieved.
	 * @param headers the map of headers, where each key is a header name and the value is a list of header values.
	 * @return a list of values for the specified header. If the header is not found, an empty list is returned.
	 */
	public static List<String> getHeaderValues(final String header, final Map<String, List<String>> headers) {
		return getHeaderValues(header, HttpHeaders.of(headers, (name, value) -> true));
	}

	/**
	 * Hide constructor.
	 */
	private HttpMessages() {
		throw Constructors.unsupportedOperationException();
	}
}
