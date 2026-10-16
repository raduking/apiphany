package org.apiphany.client.http;

import org.apiphany.ApiMessage;
import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.morphix.reflection.GenericClass;

/**
 * A {@link ContentConverter} that writes JSON request bodies with {@link JsonBuilder}. This converter does not read
 * responses. Response JSON is handled by the Jackson content converters when a JSON library is present.
 *
 * @author Radu Sebastian LAZIN
 */
public class JsonHttpContentConverter implements HttpContentConverter<String> {

	/**
	 * Constructs a new {@link JsonHttpContentConverter}. This constructor is intentionally empty, as no special
	 * initialization is required.
	 */
	public JsonHttpContentConverter() {
		// empty
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, Class)
	 */
	@Override
	public String from(final Object obj, final ApiMimeType mimeType, final Class<String> targetClass) {
		throw unsupported();
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 */
	@Override
	public String from(final Object obj, final ApiMimeType mimeType, final GenericClass<String> targetGenericClass) {
		throw unsupported();
	}

	/**
	 * @see ContentConverter#isConvertible(ApiMessage, ApiMimeType, Object, HeaderValues)
	 */
	@Override
	public <U, H> boolean isConvertible(final ApiMessage<U> message, final ApiMimeType mimeType, final H headers, final HeaderValues chain) {
		return false;
	}

	/**
	 * @see ContentConverter#isWritable(ApiMessage, ApiMimeType)
	 */
	@Override
	public <U> boolean isWritable(final ApiMessage<U> message, final ApiMimeType mimeType) {
		if (null != mimeType) {
			return ContentType.APPLICATION_JSON == mimeType.contentType();
		}
		return false;
	}

	/**
	 * @see ContentConverter#to(Object, ApiMimeType)
	 */
	@Override
	public String to(final Object body, final ApiMimeType mimeType) {
		return JsonBuilder.toJson(body);
	}

	/**
	 * Returns the exception thrown when this converter is asked to read a response.
	 *
	 * @return the unsupported operation
	 */
	private static UnsupportedOperationException unsupported() {
		return new UnsupportedOperationException("JsonHttpContentConverter writes JSON request bodies");
	}
}
