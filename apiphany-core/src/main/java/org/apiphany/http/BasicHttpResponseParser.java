package org.apiphany.http;

import java.util.Map;
import java.util.TreeMap;

/**
 * A basic HTTP response parser that can handle status line, headers, and body, including chunked transfer encoding.
 * <p>
 * This parser is designed for simplicity and may not cover all edge cases of HTTP responses.
 *
 * @author Radu Sebastian LAZIN
 */
public class BasicHttpResponseParser {

	/**
	 * HTTP status line.
	 */
	private final String statusLine;

	/**
	 * HTTP headers as a simple map of strings.
	 */
	private final Map<String, String> headers;

	/**
	 * Indicates if the response uses chunked transfer encoding.
	 */
	private final boolean chunked;

	/**
	 * Content-Length of the response body, if specified.
	 */
	private final Integer contentLength;

	/**
	 * Builder for the response body.
	 */
	private final StringBuilder bodyBuilder = new StringBuilder();

	/**
	 * Buffer for incoming data.
	 */
	private final StringBuilder buffer = new StringBuilder();

	/**
	 * Indicates if the response body has been completely received.
	 */
	private boolean complete = false;

	/**
	 * Constructs a BasicHttpResponseParser with the given raw HTTP response string.
	 *
	 * @param rawResponse the raw HTTP response string
	 */
	public BasicHttpResponseParser(final String rawResponse) {
		String[] parts = rawResponse.split("\r?\n\r?\n", 2);

		String[] headerLines = parts[0].split("\r?\n");
		this.statusLine = headerLines[0];
		this.headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		for (int i = 1; i < headerLines.length; ++i) {
			String[] header = headerLines[i].split(":\\s*", 2);
			if (header.length == 2) {
				headers.put(header[0], header[1]);
			}
		}

		this.chunked = "chunked".equalsIgnoreCase(headers.get("transfer-encoding"));
		this.contentLength = parseInt(headers.get("content-length"), 10);

		if (parts.length > 1) {
			this.buffer.append(parts[1]);
		}

		if (chunked) {
			processChunks();
		} else {
			bodyBuilder.append(buffer);
			checkCompletion();
		}
	}

	/**
	 * Returns the HTTP status line.
	 *
	 * @return the HTTP status line
	 */
	public String getStatus() {
		return statusLine;
	}

	/**
	 * Returns the HTTP status code.
	 *
	 * @return the HTTP status code
	 */
	public int getStatusCode() {
		String[] parts = statusLine.split(" ");
		if (parts.length < 2) {
			return 0;
		}
		Integer result = parseInt(parts[1], 10);
		if (null == result) {
			return 0;
		}
		return result;
	}

	/**
	 * Returns the value of the specified header.
	 *
	 * @param name the name of the header
	 * @return the value of the header, or null if not present
	 */
	public String getHeader(final String name) {
		return headers.get(name);
	}

	/**
	 * Returns the response body as a string.
	 *
	 * @return the response body
	 */
	public String getBody() {
		return bodyBuilder.toString();
	}

	/**
	 * Indicates if the response body has been completely received.
	 *
	 * @return true if the response body is complete, false otherwise
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Appends data to the response body.
	 *
	 * @param data the data to append
	 */
	public void appendData(final String data) {
		buffer.append(data);
		if (chunked) {
			processChunks();
		} else {
			bodyBuilder.append(data);
			checkCompletion();
		}
	}

	/**
	 * Parses the given string as an integer with the specified radix, returning {@code null} if the value is {@code null}
	 * or not a valid integer.
	 *
	 * @param value the string to parse
	 * @param radix the radix to use
	 * @return the parsed integer, or {@code null} if parsing fails
	 */
	private static Integer parseInt(final String value, final int radix) {
		if (null == value) {
			return null;
		}
		try {
			return Integer.parseInt(value, radix);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Parses the given string as a hexadecimal integer, returning {@code null} if the value is {@code null}, not a valid
	 * integer, or negative.
	 *
	 * @param value the string to parse
	 * @return the parsed integer, or {@code null} if parsing fails
	 */
	private static Integer parseHexInt(final String value) {
		Integer result = parseInt(value, 16);
		if (null != result && result < 0) {
			return null;
		}
		return result;
	}

	/**
	 * Processes chunked transfer encoding data.
	 */
	private void processChunks() {
		while (true) {
			int endIndex = buffer.indexOf(HttpMessages.CRLF);
			if (endIndex == -1) {
				return; // not enough data for size
			}

			String sizeStr = buffer.substring(0, endIndex).trim();
			Integer chunkSize = parseHexInt(sizeStr);
			if (null == chunkSize) {
				return; // malformed chunk size
			}

			if (buffer.length() < endIndex + 2 + chunkSize + 2) {
				return; // wait for more data
			}

			if (chunkSize == 0) {
				complete = true;
				buffer.setLength(0); // discard trailing headers if any
				return;
			}

			bodyBuilder.append(buffer, endIndex + 2, endIndex + 2 + chunkSize);
			buffer.delete(0, endIndex + 2 + chunkSize + 2);
		}
	}

	/**
	 * Checks if the response body is complete based on Content-Length.
	 */
	private void checkCompletion() {
		if (null != contentLength && bodyBuilder.length() >= contentLength) {
			complete = true;
		}
	}
}
