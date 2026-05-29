package org.apiphany.multipart;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apiphany.http.HttpContentType;
import org.apiphany.io.BinaryRepresentable;
import org.apiphany.io.ContentType;
import org.apiphany.lang.Bytes;
import org.apiphany.lang.Strings;
import org.morphix.lang.collections.Lists;

/**
 * Represents a generic multipart message as defined by MIME. A multipart message consists of a boundary and an ordered
 * collection of parts.
 *
 * @author Radu Sebastian LAZIN
 */
public class MultipartMessage implements BinaryRepresentable {

	/**
	 * The boundary string that separates the parts in the multipart message.
	 */
	private final MultipartBoundary boundary;

	/**
	 * The ordered collection of parts contained in the multipart message.
	 */
	private final List<MultipartPart<?>> parts;

	/**
	 * Constructs a new {@code MultipartMessage} with the specified boundary and parts.
	 *
	 * @param boundary the boundary string that separates the parts in the multipart message
	 * @param parts the ordered collection of parts contained in the multipart message
	 * @throws NullPointerException if {@code boundary} is {@code null}
	 */
	public MultipartMessage(final MultipartBoundary boundary, final List<MultipartPart<?>> parts) {
		this.boundary = Objects.requireNonNull(boundary, "boundary must not be null");
		this.parts = List.copyOf(Lists.safe(parts));
	}

	/**
	 * Returns the boundary string that separates the parts in the multipart message.
	 *
	 * @return the boundary string
	 */
	public MultipartBoundary getBoundary() {
		return boundary;
	}

	/**
	 * Returns an unmodifiable list of the parts contained in the multipart message.
	 *
	 * @return the list of parts
	 */
	public List<MultipartPart<?>> getParts() {
		return parts;
	}

	/**
	 * @see BinaryRepresentable#toByteArray()
	 */
	@Override
	public byte[] toByteArray() {
		return Bytes.capture(
				os -> MultipartEncoder.write(os, this),
				e -> new IllegalStateException("Unexpected I/O error while encoding multipart body", e));
	}

	/**
	 * Returns a string representation of this multipart body using the specified character set. The multipart body is
	 * encoded to bytes and then decoded to a string using the given character set. This is useful for debugging purposes,
	 * but should not be used for actual processing of multipart bodies, as the encoding may not be correct for all parts.
	 *
	 * @param charset the character set to use for decoding the multipart body
	 * @return a string representation of this multipart body
	 */
	public String toString(final Charset charset) {
		return Strings.toString(toByteArray(), charset);
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
	 * Creates a new {@code Builder} instance for constructing a {@code MultipartMessage}.
	 *
	 * @return a new {@code Builder} instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder class for constructing instances of {@code MultipartMessage} using a fluent API.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static final class Builder {

		/**
		 * The boundary string that separates the parts in the multipart message. This field is required and must be set before
		 * building the message.
		 */
		private MultipartBoundary boundary;

		/**
		 * The ordered collection of parts contained in the multipart message. This field is optional and can be empty.
		 */
		private final List<MultipartPart<?>> parts = new ArrayList<>();

		/**
		 * Private constructor.
		 */
		private Builder() {
			// hide constructor
		}

		/**
		 * Sets the boundary string that separates the parts in the multipart message. This method is required and must be
		 * called before building the message.
		 *
		 * @param boundary the boundary string to set
		 * @return this builder instance for chaining
		 */
		public Builder boundary(final MultipartBoundary boundary) {
			this.boundary = boundary;
			return this;
		}

		/**
		 * Sets the boundary string using a simple string value. This method is a convenience overload that creates a
		 * {@code MultipartBoundary} instance from the provided string.
		 *
		 * @param boundary the boundary string to set
		 * @return this builder instance for chaining
		 */
		public Builder boundary(final String boundary) {
			return boundary(new MultipartBoundary(boundary));
		}

		/**
		 * Sets a random boundary string using a UUID-based value. This method is a convenience overload that generates a random
		 * boundary string and sets it for the multipart message.
		 *
		 * @return this builder instance for chaining
		 */
		public Builder randomBoundary() {
			return boundary(MultipartBoundary.random());
		}

		/**
		 * Adds a part to the multipart message. This method is optional and can be called multiple times to add multiple parts.
		 *
		 * @param part the {@code MultipartPart} to add
		 * @return this builder instance for chaining
		 */
		public Builder part(final MultipartPart<?> part) {
			parts.add(Objects.requireNonNull(part, "part must not be null"));
			return this;
		}

		/**
		 * Adds a field multipart part.
		 *
		 * @param <T> field value type
		 *
		 * @param name field name
		 * @param value field value
		 * @return this builder instance for chaining
		 */
		public <T> Builder field(final String name, final T value) {
			return part(MultipartPart.ofField(name, value));
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
			return part(MultipartPart.ofFile(name, filename, contentType, data));
		}

		/**
		 * Builds and returns a new {@code MultipartMessage} instance using the configured boundary and parts. The boundary must
		 * be set before calling this method, otherwise an exception will be thrown.
		 *
		 * @return a new {@code MultipartMessage} instance
		 * @throws IllegalStateException if the boundary is not set
		 */
		public MultipartMessage build() {
			return new MultipartMessage(boundary, parts);
		}
	}
}
