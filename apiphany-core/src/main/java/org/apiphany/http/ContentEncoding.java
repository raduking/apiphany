package org.apiphany.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apiphany.io.gzip.GZip;
import org.apiphany.lang.collections.Lists;
import org.morphix.lang.Enums;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.ToStringFunction;
import org.morphix.reflection.Constructors;

/**
 * Represents the Content-Encoding HTTP header values. Used to indicate what content encodings have been applied to the
 * payload.
 *
 * @author Radu Sebastian LAZIN
 */
public enum ContentEncoding {

	/**
	 * Indicates no encoding transformation (default). The {@code identity} encoding is always acceptable and should be
	 * supported by all implementations.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#section-8.4.1">RFC 9110 Section 8.4.1</a>
	 */
	IDENTITY(Value.IDENTITY),

	/**
	 * The {@code gzip} encoding format (LZ77 + CRC32). This is the most widely supported compression format for HTTP.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc1952">RFC 1952: GZIP File Format Specification</a>
	 */
	GZIP(Value.GZIP) {

		/**
		 * Decodes the given input stream or byte array using GZIP decompression.
		 *
		 * @param <T> body type
		 *
		 * @param body compressed body to decode, can be an InputStream or a byte array
		 * @return de-compressed body that decodes the original body using GZIP decompression
		 * @throws IllegalStateException if any error occurs during decompression
		 */
		@Override
		public <T> T decode(final T body) {
			try {
				return GZip.decompress(body);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to decode response body with encoding: " + this, e);
			}
		}
	},

	/**
	 * The {@code zlib} format (RFC 1950) with {@code deflate} compression (RFC 1951). Note: Some implementations
	 * incorrectly use raw {@code deflate} without {@code zlib} headers.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc1950">RFC 1950: ZLIB Compressed Data Format</a>
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc1951">RFC 1951: DEFLATE Compressed Data Format</a>
	 */
	DEFLATE(Value.DEFLATE),

	/**
	 * Brotli compressed data format (lossless compression algorithm). Provides better compression ratios than gzip at
	 * similar speeds.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc7932">RFC 7932: Brotli Compressed Data Format</a>
	 */
	BR(Value.BR),

	/**
	 * Zstandard compression format developed by Facebook. Provides excellent compression speed/ratio trade-off. While not
	 * yet IANA-registered, it's widely supported on modern web servers.
	 *
	 * @see <a href="https://facebook.github.io/zstd/">Zstandard Documentation</a>
	 */
	ZSTD(Value.ZSTD),

	/**
	 * The UNIX "compress" program format (LZW algorithm). This encoding is largely obsolete and not widely supported in
	 * modern HTTP implementations.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#section-8.4.1">RFC 9110 Section 8.4.1</a>
	 */
	COMPRESS(Value.COMPRESS),

	/**
	 * LZ4 compression format (extremely fast compression). Primarily used in specialized high-performance applications. Not
	 * IANA-registered for HTTP.
	 *
	 * @see <a href="https://lz4.github.io/lz4/">LZ4 Documentation</a>
	 */
	LZ4(Value.LZ4),

	/**
	 * XZ compression format (LZMA2 algorithm). Provides excellent compression ratios but is slow. Rarely used in HTTP, more
	 * common in file compression.
	 *
	 * @see <a href="https://tukaani.org/xz/format.html">XZ File Format Specification</a>
	 */
	XZ(Value.XZ),

	/**
	 * Bzip2 compression format (Burrows-Wheeler algorithm). Not commonly used in HTTP due to high CPU requirements.
	 *
	 * @see <a href="https://sourceware.org/bzip2/">bzip2 Documentation</a>
	 */
	BZIP2(Value.BZIP2);

	/**
	 * A utility class containing string constants for the content encoding values. This allows for easy reference to the
	 * standard content encoding strings without hard coding them throughout the code base.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Value {

		/**
		 * The string value for the {@code identity} content encoding.
		 */
		public static final String IDENTITY = "identity";

		/**
		 * The string value for the {@code gzip} content encoding.
		 */
		public static final String GZIP = "gzip";

		/**
		 * The string value for the {@code deflate} content encoding.
		 */
		public static final String DEFLATE = "deflate";

		/**
		 * The string value for the {@code br} content encoding.
		 */
		public static final String BR = "br";

		/**
		 * The string value for the {@code zstd} content encoding.
		 */
		public static final String ZSTD = "zstd";

		/**
		 * The string value for the {@code compress} content encoding.
		 */
		public static final String COMPRESS = "compress";

		/**
		 * The string value for the {@code lz4} content encoding.
		 */
		public static final String LZ4 = "lz4";

		/**
		 * The string value for the {@code xz} content encoding.
		 */
		public static final String XZ = "xz";

		/**
		 * The string value for the {@code bzip2} content encoding.
		 */
		public static final String BZIP2 = "bzip2";

		/**
		 * Hide constructor.
		 */
		private Value() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, ContentEncoding> NAME_MAP = Enums.buildNameMap(values(), ToStringFunction.toLowerCase());

	/**
	 * Content encoding value.
	 */
	private final String value;

	/**
	 * Constructs a content encoding constant.
	 *
	 * @param value the string representation of the constant
	 */
	ContentEncoding(final String value) {
		this.value = value;
	}

	/**
	 * Decodes the given input stream according to the content encoding. The default implementation throws an
	 * {@link UnsupportedOperationException} since the actual decoding logic is implemented in each specific enum constant.
	 *
	 * @param <T> body type
	 *
	 * @param body the body to decode
	 * @return a decoded body that decodes the original body according to the content encoding
	 * @throws UnsupportedOperationException if the content encoding is not supported or if the decoding logic is not
	 *     implemented for this encoding
	 */
	public <T> T decode(final T body) {
		throw new UnsupportedOperationException("Content encoding " + this + " is not supported!");
	}

	/**
	 * Returns a {@link ContentEncoding} enum from a {@link String}.
	 *
	 * @param encoding the string representation of the content encoding
	 * @return a content encoding enum
	 */
	public static ContentEncoding fromString(final String encoding) {
		return Enums.fromString(Objects.requireNonNull(encoding).toLowerCase(), NAME_MAP, values());
	}

	/**
	 * Returns a {@link ContentEncoding} enum from a {@link String}, or a default value if the string does not match any
	 * enum value.
	 *
	 * @param encoding the string representation of the content encoding
	 * @param defaultValueSupplier supplier for the default value
	 * @return a content encoding enum, or the default value if no match is found
	 */
	public static ContentEncoding fromString(final String encoding, final Supplier<ContentEncoding> defaultValueSupplier) {
		return Enums.from(encoding, NAME_MAP, defaultValueSupplier);
	}

	/**
	 * Returns true if the given string matches the enum value ignoring the case, false otherwise. The HTTP headers are
	 * case-insensitive.
	 *
	 * @param encoding the string representation of the content encoding
	 * @return true if the given string matches the enum value ignoring the case, false otherwise.
	 */
	public boolean matches(final String encoding) {
		return value().equalsIgnoreCase(encoding);
	}

	/**
	 * Returns the string value.
	 *
	 * @return the string value
	 */
	public String value() {
		return value;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return value();
	}

	/**
	 * Parses the given list of strings and returns the first matching {@link ContentEncoding} enum. If none of the strings
	 * match, null is returned.
	 *
	 * @param values the list of strings to parse
	 * @return the first matching content encoding enum, or null if none match
	 */
	public static ContentEncoding parseFirst(final List<String> values) {
		if (Lists.isEmpty(values)) {
			return null;
		}
		for (String value : values) {
			ContentEncoding encoding = fromString(value, Nullables.supplyNull());
			if (null != encoding) {
				return encoding;
			}
		}
		return null;
	}

	/**
	 * Parses the given list of strings and returns a list of matching {@link ContentEncoding} enums. If none of the strings
	 * match, an empty list is returned.
	 *
	 * @param values the list of strings to parse
	 * @return a list of matching content encoding enums, or an empty list if none match
	 */
	public static List<ContentEncoding> parseAll(final List<String> values) {
		if (Lists.isEmpty(values)) {
			return Collections.emptyList();
		}
		List<ContentEncoding> encodings = new ArrayList<>(values.size());
		for (String value : values) {
			ContentEncoding encoding = fromString(value, Nullables.supplyNull());
			if (null != encoding) {
				encodings.add(encoding);
			}
		}
		return encodings;
	}

	/**
	 * Decodes the body based on the given content encoding list. The body is decoded in the reverse order of the content
	 * encodings, meaning that the last encoding in the list is decoded first. If the body is null or the content encoding
	 * list is empty, the original body is returned without any decoding.
	 *
	 * @param <T> body type
	 *
	 * @param body the body
	 * @param encodings content encoding list in the order they were applied to the body
	 * @return decoded body
	 */
	public static <T> T decodeBody(final T body, final List<ContentEncoding> encodings) {
		if (null == body || Lists.isEmpty(encodings)) {
			return body;
		}
		if (body instanceof InputStream || body instanceof byte[]) {
			T result = body;
			for (ContentEncoding encoding : encodings.reversed()) {
				result = encoding.decode(result);
			}
			return result;
		}
		return body;
	}
}
