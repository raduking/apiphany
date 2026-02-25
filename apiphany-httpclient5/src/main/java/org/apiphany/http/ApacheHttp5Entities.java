package org.apiphany.http;

import java.io.InputStream;

import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.morphix.lang.function.ThrowingSupplier;

/**
 * Utility class for working with HTTP entities. Provides methods to convert HTTP entities to various formats, such as
 * input streams and byte arrays, which can be used by content converters to read response bodies without loading them
 * entirely into memory.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApacheHttp5Entities {

	/**
	 * Return the content of the HTTP entity as an input stream. This method is used to convert the response body to the
	 * target type using the content converters. The content converters can use the input stream to read the response body
	 * without loading it entirely into memory, which is useful for large responses.
	 *
	 * @param httpEntity the HTTP entity to read the content from
	 * @return an input stream containing the content of the HTTP entity
	 */
	public static InputStream toInputStream(final HttpEntity httpEntity) {
		return ThrowingSupplier.unchecked(httpEntity::getContent).get();
	}

	/**
	 * Return the content of the HTTP entity as a byte array. This method is used to convert the response body to the target
	 * type using the content converters. The content converters can use the byte array to read the response body without
	 * loading it entirely into memory, which is useful for large responses.
	 *
	 * @param httpEntity the HTTP entity to read the content from
	 * @return a byte array containing the content of the HTTP entity
	 */
	public static byte[] toByteArray(final HttpEntity httpEntity) {
		return ThrowingSupplier.unchecked(() -> EntityUtils.toByteArray(httpEntity)).get();
	}
}
