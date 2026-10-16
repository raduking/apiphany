package org.apiphany.client.http;

import org.apiphany.ApiMessage;
import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.morphix.convert.ObjectConverterException;
import org.morphix.reflection.GenericClass;

/**
 * A {@link ContentConverter} that writes JSON request bodies with {@link JsonBuilder}. This converter does not read
 * responses. Response JSON is handled by the Jackson content converters when a JSON library is present.
 *
 * @param <T> the type of the object to which the JSON content will be converted.
 *
 * @author Radu Sebastian LAZIN
 */
public class JsonHttpContentConverter<T> implements HttpContentConverter<T> {

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
	public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
		T result = JsonBuilder.fromJson(obj, targetClass);
		if (null == result && null != obj) {
			throw new ObjectConverterException("Error converting JSON response to " + targetClass.getName());
		}
		return result;
	}

	/**
	 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
	 */
	@Override
	public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
		T result = JsonBuilder.fromJson(obj, targetGenericClass);
		if (null == result && null != obj) {
			throw new ObjectConverterException("Error converting JSON response to " + targetGenericClass.getType().getTypeName());
		}
		return result;
	}

	/**
	 * @see ContentConverter#isConvertible(ApiMessage, ApiMimeType, Object, HeaderValues)
	 */
	@Override
	public <U, H> boolean isConvertible(final ApiMessage<U> message, final ApiMimeType mimeType, final H headers, final HeaderValues chain) {
		if (null != mimeType) {
			return ContentType.APPLICATION_JSON == mimeType.contentType();
		}
		return false;
	}

	/**
	 * @see ContentConverter#toString(Object, ApiMimeType)
	 */
	@Override
	public String toString(final Object body, final ApiMimeType mimeType) {
		return JsonBuilder.toJson(body);
	}
}
