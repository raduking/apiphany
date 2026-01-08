package org.apiphany.http;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apiphany.lang.collections.Lists;
import org.morphix.lang.Enums;

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
	IDENTITY("identity"),

	/**
	 * The {@code gzip} encoding format (LZ77 + CRC32). This is the most widely supported compression format for HTTP.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc1952">RFC 1952: GZIP File Format Specification</a>
	 */
	GZIP("gzip"),

	/**
	 * The {@code zlib} format (RFC 1950) with {@code deflate} compression (RFC 1951). Note: Some implementations
	 * incorrectly use raw {@code deflate} without {@code zlib} headers.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc1950">RFC 1950: ZLIB Compressed Data Format</a>
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc1951">RFC 1951: DEFLATE Compressed Data Format</a>
	 */
	DEFLATE("deflate"),

	/**
	 * Brotli compressed data format (lossless compression algorithm). Provides better compression ratios than gzip at
	 * similar speeds.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc7932">RFC 7932: Brotli Compressed Data Format</a>
	 */
	BR("br"),

	/**
	 * Zstandard compression format developed by Facebook. Provides excellent compression speed/ratio trade-off. While not
	 * yet IANA-registered, it's widely supported on modern web servers.
	 *
	 * @see <a href="https://facebook.github.io/zstd/">Zstandard Documentation</a>
	 */
	ZSTD("zstd"),

	/**
	 * The UNIX "compress" program format (LZW algorithm). This encoding is largely obsolete and not widely supported in
	 * modern HTTP implementations.
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#section-8.4.1">RFC 9110 Section 8.4.1</a>
	 */
	COMPRESS("compress"),

	/**
	 * LZ4 compression format (extremely fast compression). Primarily used in specialized high-performance applications. Not
	 * IANA-registered for HTTP.
	 *
	 * @see <a href="https://lz4.github.io/lz4/">LZ4 Documentation</a>
	 */
	LZ4("lz4"),

	/**
	 * XZ compression format (LZMA2 algorithm). Provides excellent compression ratios but is slow. Rarely used in HTTP, more
	 * common in file compression.
	 *
	 * @see <a href="https://tukaani.org/xz/format.html">XZ File Format Specification</a>
	 */
	XZ("xz"),

	/**
	 * Bzip2 compression format (Burrows-Wheeler algorithm). Not commonly used in HTTP due to high CPU requirements.
	 *
	 * @see <a href="https://sourceware.org/bzip2/">bzip2 Documentation</a>
	 */
	BZIP2("bzip2");

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, ContentEncoding> NAME_MAP = Enums.buildNameMap(values(), encoding -> encoding.toString().toLowerCase());

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
	 * Returns a {@link ContentEncoding} enum from a {@link String}.
	 *
	 * @param encoding the string representation of the content encoding
	 * @return a content encoding enum
	 */
	public static ContentEncoding fromString(final String encoding) {
		return Enums.fromString(Objects.requireNonNull(encoding).toLowerCase(), NAME_MAP, values());
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
	public static ContentEncoding parse(final List<String> values) {
		if (Lists.isEmpty(values)) {
			return null;
		}
		for (String value : values) {
			try {
				return fromString(value);
			} catch (IllegalArgumentException ex) {
				// continue
			}
		}
		return null;
	}
}
