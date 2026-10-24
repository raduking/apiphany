package org.apiphany.http;

import java.io.InputStream;

import org.apiphany.io.IOStreams;
import org.apiphany.lang.Bytes;
import org.morphix.lang.function.ThrowingSupplier;
import org.morphix.reflection.Constructors;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Utility class for working with OkHttp request/response bodies. Provides methods to convert response bodies to various
 * formats, such as input streams and byte arrays, which can be used by content converters to read response bodies
 * without loading them entirely into memory.
 *
 * @author Radu Sebastian LAZIN
 */
public class OkHttpEntities {

	/**
	 * Return the content of the response body as an input stream. This method is used to convert the response body to the
	 * target type using the content converters. The content converters can use the input stream to read the response body
	 * without loading it entirely into memory, which is useful for large responses.
	 *
	 * @param responseBody the response body to read the content from
	 * @return an input stream containing the content of the response body
	 */
	public static InputStream toInputStream(final ResponseBody responseBody) {
		if (null == responseBody) {
			return InputStream.nullInputStream();
		}
		return ThrowingSupplier.unchecked(responseBody::byteStream).get();
	}

	/**
	 * Return the response body content as an input stream while keeping response lifecycle bound to stream close.
	 *
	 * @param response the HTTP response to read from
	 * @return an input stream backed by the response
	 */
	@SuppressWarnings("resource")
	public static InputStream toInputStream(final Response response) {
		if (null == response || null == response.body()) {
			return InputStream.nullInputStream();
		}
		return CloseableOkHttpResponseInputStream.of(response);
	}

	/**
	 * Return the content of the response body as a byte array. This method is used to convert the response body to the
	 * target type using the content converters.
	 *
	 * @param responseBody the response body to read the content from
	 * @return a byte array containing the content of the response body
	 */
	public static byte[] toByteArray(final ResponseBody responseBody) {
		return toByteArray(responseBody, IOStreams.MAX_BUFFER_SIZE);
	}

	/**
	 * Return the content of the response body as a byte array with an upper size limit.
	 *
	 * @param responseBody the response body to read the content from
	 * @param maxBytes maximum number of bytes allowed
	 * @return a byte array containing the content of the response body
	 */
	public static byte[] toByteArray(final ResponseBody responseBody, final int maxBytes) {
		if (null == responseBody) {
			return Bytes.EMPTY;
		}
		try (InputStream contentStream = toInputStream(responseBody)) {
			return IOStreams.toByteArray(contentStream, maxBytes);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to read response body content as byte array: " + e.getMessage(), e);
		}
	}

	/**
	 * Return the content of the HTTP response as a byte array with an upper size limit. The caller is responsible for
	 * closing the response after reading the content.
	 *
	 * @param response the HTTP response to read from
	 * @param maxBytes maximum number of bytes allowed
	 * @return a byte array containing the content of the response body
	 */
	@SuppressWarnings("resource")
	public static byte[] toByteArray(final Response response, final int maxBytes) {
		if (null == response) {
			return Bytes.EMPTY;
		}
		return toByteArray(response.body(), maxBytes);
	}

	/**
	 * Create a new request body from the given input stream and content type. This method is used to create a new request
	 * body that can be sent as the request body when sending HTTP requests. The created request body will be
	 * non-repeatable, meaning it can only be written once, which is useful for streaming large request bodies.
	 *
	 * @param content the input stream containing the content of the request body
	 * @param mediaType the content type of the request body
	 * @return a new request body containing the given content and content type
	 */
	public static RequestBody create(final InputStream content, final MediaType mediaType) {
		return new OneShotRequestBody(content, mediaType);
	}

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private OkHttpEntities() {
		throw Constructors.unsupportedOperationException();
	}
}
