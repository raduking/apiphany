package org.apiphany.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiMessage;
import org.apiphany.header.Headers;
import org.apiphany.io.ChunkedBinary;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Bytes;
import org.apiphany.lang.Strings;
import org.morphix.lang.Ids;
import org.morphix.lang.Ids.UUIDStyle;
import org.morphix.lang.Nullables;
import org.morphix.lang.collections.Maps;

/**
 * Namespace class for multipart form-data constructs.
 *
 * @author Radu Sebastian LAZIN
 */
public interface Multipart {

	/**
	 * Represents a single part in a multipart request body. Each part consists of headers (such as Content-Disposition and
	 * Content-Type) and a byte array body.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Part extends ApiMessage<byte[]> {

		/**
		 * Constructs a multipart part with the given headers and body. The headers map must not be {@code null}, but can be
		 * empty. The body can be {@code null} or empty.
		 *
		 * @param headers the headers for this part
		 * @param body the body of this part as a byte array
		 */
		public Part(final Map<String, List<String>> headers, final byte[] body) {
			super(body, headers);
		}

		/**
		 * Creates a simple form field part.
		 *
		 * @param name the field name
		 * @param value the field value
		 * @return a new multipart part
		 */
		public static Part ofField(final String name, final String value) {
			Map<String, List<String>> headers = new LinkedHashMap<>();
			Headers.addTo(headers, HttpHeader.CONTENT_DISPOSITION, HttpHeaderValues.FORM_DATA + "; name=\"" + name + "\"");
			byte[] bodyBytes = null != value ? value.getBytes(StandardCharsets.US_ASCII) : Bytes.EMPTY;
			return new Part(headers, bodyBytes);
		}

		/**
		 * Creates a file upload part.
		 *
		 * @param name the field name
		 * @param filename the original filename
		 * @param contentType the content type of the file
		 * @param data the file data
		 * @return a new multipart part
		 */
		public static Part ofFile(final String name, final String filename, final String contentType, final byte[] data) {
			Map<String, List<String>> headers = new LinkedHashMap<>();
			String disposition = HttpHeaderValues.FORM_DATA + "; name=\"" + name + "\"";
			if (Strings.isNotBlank(filename)) {
				disposition += "; filename=\"" + filename + "\"";
			}
			Headers.addTo(headers, HttpHeader.CONTENT_DISPOSITION, disposition);
			String actualContentType = Nullables.nonNullOrDefault(contentType, ContentType.Value.APPLICATION_OCTET_STREAM);
			Headers.addTo(headers, HttpHeader.CONTENT_TYPE, actualContentType);
			return new Part(headers, null != data ? data : Bytes.EMPTY);
		}
	}

	/**
	 * Represents a multipart request body with a boundary and a list of parts. The body can be encoded to bytes following
	 * the multipart/form-data format defined in RFC 2046.
	 * <p>
	 * Use the {@link Body.Builder} to construct a multipart body with fields and files.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Body implements ChunkedBinary {

		/**
		 * Carriage Return Line Feed as bytes, used to separate lines in the multipart body.
		 */
		private static final byte[] CRLF = HttpMessages.CRLF.getBytes(StandardCharsets.US_ASCII);

		/**
		 * The byte array representing the "--" prefix used in multipart boundaries.
		 */
		private static final byte[] DASH_BYTES = "--".getBytes(StandardCharsets.US_ASCII);

		/**
		 * The boundary string that separates parts in the multipart body. This must be unique and should not appear in the
		 * content of any part.
		 */
		private final String boundary;

		/**
		 * The list of parts included in this multipart body. Each part consists of headers and a byte array body.
		 */
		private final List<Part> parts;

		/**
		 * The boundary string as bytes, pre-encoded for efficient writing to output streams.
		 */
		private final byte[] boundaryBytes;

		/**
		 * Constructs a multipart body with the given boundary and parts. The boundary must not be {@code null} or empty, and
		 * the parts list must not be {@code null}.
		 *
		 * @param boundary the boundary string for this multipart body
		 * @param parts the list of parts included in this multipart body
		 */
		private Body(final String boundary, final List<Part> parts) {
			this.boundary = boundary;
			this.parts = parts;
			this.boundaryBytes = boundary.getBytes(StandardCharsets.US_ASCII);
		}

		/**
		 * Creates a new builder for constructing a multipart body.
		 *
		 * @return a new {@link Body.Builder} instance
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * Returns the boundary string used in this multipart body.
		 *
		 * @return the boundary string
		 */
		@Override
		public String getBoundary() {
			return boundary;
		}

		/**
		 * Returns the list of parts included in this multipart body.
		 *
		 * @return the list of parts
		 */
		public List<Part> getParts() {
			return parts;
		}

		/**
		 * Returns the multipart content type value.
		 *
		 * @return the content type header value
		 */
		public String getContentTypeValue() {
			return ContentType.Value.MULTIPART_FORM_DATA
					+ "; "
					+ HttpContentType.Param.BOUNDARY
					+ "="
					+ boundary;
		}

		/**
		 * Writes this multipart body to the given output stream.
		 *
		 * @param out the destination stream
		 * @throws IOException if writing fails
		 */
		public void writeTo(final OutputStream out) throws IOException {
			for (Part part : parts) {
				out.write(DASH_BYTES);
				out.write(boundaryBytes);
				out.write(CRLF);
				writeHeaders(out, part);
				out.write(CRLF);
				byte[] body = part.getBody();
				if (Bytes.isNotEmpty(body)) {
					out.write(body);
				}
				out.write(CRLF);
			}
			out.write(DASH_BYTES);
			out.write(boundaryBytes);
			out.write(DASH_BYTES);
		}

		/**
		 * Writes the headers of a multipart part to the output stream.
		 *
		 * @param out the destination stream
		 * @param part the multipart part whose headers are to be written
		 * @throws IOException if writing fails
		 */
		private static void writeHeaders(final OutputStream out, final Part part) throws IOException {
			Map<String, List<String>> headers = part.getHeaders();
			if (Maps.isEmpty(headers)) {
				return;
			}
			for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
				String headerName = entry.getKey();
				for (String value : entry.getValue()) {
					writeAscii(out, headerName);
					writeAscii(out, ": ");
					writeAscii(out, value);
					out.write(CRLF);
				}
			}
		}

		/**
		 * Writes an ASCII string to the output stream.
		 *
		 * @param out the destination stream
		 * @param value the string to write
		 * @throws IOException if writing fails
		 */
		private static void writeAscii(final OutputStream out, final String value) throws IOException {
			out.write(value.getBytes(StandardCharsets.US_ASCII));
		}

		/**
		 * Encodes this multipart body as bytes.
		 *
		 * @return the encoded multipart body
		 */
		@Override
		public byte[] toByteArray() {
			return Bytes.from(this::writeTo, e -> new IllegalStateException("Unexpected I/O error while encoding multipart body", e));
		}

		/**
		 * Builder class for constructing multipart bodies with a fluent API. The builder allows setting a custom boundary and
		 * adding fields and files as parts.
		 *
		 * @author Radu Sebastian LAZIN
		 */
		public static final class Builder {

			/**
			 * The boundary string for this multipart body. If not set, a random boundary will be generated when building the body.
			 */
			private String boundary;

			/**
			 * The list of parts to be included in the multipart body. This list is mutable during building, but will be copied to
			 * an immutable list when the body is built.
			 */
			private final List<Part> parts = new ArrayList<>();

			/**
			 * Constructs a new builder instance. The constructor is private to enforce the use of the {@link Body#builder()} method
			 * for creating builder instances.
			 */
			private Builder() {
				// hide constructor
			}

			/**
			 * Sets a custom boundary string for the multipart body. If not set, a random boundary will be generated when building
			 * the body.
			 *
			 * @param boundary the boundary string to use
			 * @return this builder instance for chaining
			 */
			public Builder boundary(final String boundary) {
				this.boundary = boundary;
				return this;
			}

			/**
			 * Adds a simple form field part to the multipart body.
			 *
			 * @param name the field name
			 * @param value the field value
			 * @return this builder instance for chaining
			 */
			public Builder field(final String name, final String value) {
				parts.add(Part.ofField(name, value));
				return this;
			}

			/**
			 * Adds a file upload part to the multipart body.
			 *
			 * @param name the field name
			 * @param filename the original filename
			 * @param contentType the content type of the file
			 * @param data the file data
			 * @return this builder instance for chaining
			 */
			public Builder file(final String name, final String filename, final String contentType, final byte[] data) {
				parts.add(Part.ofFile(name, filename, contentType, data));
				return this;
			}

			/**
			 * Adds a custom multipart part to the body.
			 *
			 * @param part the multipart part to add
			 * @return this builder instance for chaining
			 */
			public Builder part(final Part part) {
				parts.add(part);
				return this;
			}

			/**
			 * Builds the multipart body with the specified boundary and parts. If no boundary was set, a random boundary will be
			 * generated. The list of parts will be copied to an immutable list in the resulting body.
			 *
			 * @return the constructed multipart body
			 */
			public Body build() {
				String actualBoundary = Nullables.nonNullOrDefault(boundary, Builder::generateBoundary);
				return new Body(actualBoundary, List.copyOf(parts));
			}

			/**
			 * Generates a random boundary string using a UUID without dashes. The generated boundary is prefixed with "----" to
			 * reduce the likelihood of collisions with content in the parts.
			 *
			 * @return a random boundary string
			 */
			private static String generateBoundary() {
				return "----" + Ids.generateUUIDString(UUIDStyle.NO_DASHES);
			}
		}
	}
}
