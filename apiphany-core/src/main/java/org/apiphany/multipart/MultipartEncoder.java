package org.apiphany.multipart;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apiphany.lang.Bytes;
import org.morphix.lang.collections.Maps;

/**
 * Utility class for encoding multipart messages according to the MIME specification. This class provides methods to
 * write a {@link MultipartMessage} to an {@link OutputStream} in the correct format, including boundaries, headers, and
 * body content.
 *
 * @author Radu Sebastian LAZIN
 */
public class MultipartEncoder {

	/**
	 * The CRLF sequence used to separate headers and body, and to separate parts in the multipart message. This is defined
	 * as per the MIME specification.
	 */
	private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.US_ASCII);

	/**
	 * The sequence used to indicate the start of a boundary in the multipart message. This is defined as per the MIME
	 * specification.
	 */
	private static final byte[] DASH = "--".getBytes(StandardCharsets.US_ASCII);

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private MultipartEncoder() {
		// utility
	}

	/**
	 * Writes the given {@link MultipartMessage} to the specified {@link OutputStream} in the correct multipart format. This
	 * method handles writing the boundaries, headers, and body content for each part of the message.
	 * <p>
	 * The output will be formatted according to the MIME specification for multipart messages.
	 *
	 * @param message the multipart message to write
	 * @param out the output stream to write the message to
	 * @throws IOException if an I/O error occurs while writing to the output stream
	 */
	public static void write(final MultipartMessage message, final OutputStream out) throws IOException {
		byte[] boundaryBytes = message.getBoundary().value().getBytes(StandardCharsets.US_ASCII);
		for (MultipartPart<?> part : message.getParts()) {
			out.write(DASH);
			out.write(boundaryBytes);
			out.write(CRLF);
			writeHeaders(out, part.getHeaders());
			out.write(CRLF);
			Object body = part.getBody();
			if (body instanceof byte[] bytes && Bytes.isNotEmpty(bytes)) {
				out.write(bytes);
			}
			out.write(CRLF);
		}
		// closing boundary
		out.write(DASH);
		out.write(boundaryBytes);
		out.write(DASH);
	}

	/**
	 * Writes the headers of a multipart part to the specified output stream. Each header is written in the format
	 * "Header-Name: Header-Value" followed by a CRLF sequence.
	 * <p>
	 * If there are multiple values for the same header name, each value is written on a separate line with the same header
	 * name.
	 *
	 * @param out the output stream to write the headers to
	 * @param headers the map of headers to write, where the keys are header names and the values are lists of header values
	 * @throws IOException if an I/O error occurs while writing to the output stream
	 */
	private static void writeHeaders(final OutputStream out, final Map<String, List<String>> headers) throws IOException {
		if (Maps.isEmpty(headers)) {
			return;
		}
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			String name = entry.getKey();
			for (String value : entry.getValue()) {
				writeAscii(out, name);
				writeAscii(out, ": ");
				writeAscii(out, value);
				out.write(CRLF);
			}
		}
	}

	/**
	 * Writes the given string to the output stream using ASCII encoding. This method is used for writing header names and
	 * values, which must be ASCII according to the MIME specification.
	 * <p>
	 * If the string contains non-ASCII characters, they will be replaced with a placeholder character.
	 *
	 * @param out the output stream to write to
	 * @param value the string value to write
	 * @throws IOException if an I/O error occurs while writing to the output stream
	 */
	private static void writeAscii(final OutputStream out, final String value) throws IOException {
		out.write(value.getBytes(StandardCharsets.US_ASCII));
	}
}
