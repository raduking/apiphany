package org.apiphany.json.jackson2;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.http.JsonHttpContentConverter;
import org.apiphany.json.JsonBuilder;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Resolves a Jackson 2 {@code MappingJackson2HttpMessageConverter} to a {@link ContentConverter} that reads and writes
 * JSON with that converter's {@link ObjectMapper}.
 * <p>
 * This class uses Jackson 2 and Spring's Jackson 2 types directly. {@link org.apiphany.http.SpringJson} only calls
 * {@link #converter(Object)} after verifying {@code Jackson2Library.isPresent()}, so this class is never loaded unless
 * Jackson 2 is on the classpath.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Jackson2SpringJson {

	/**
	 * Returns a {@link ContentConverter} that reads and writes JSON with the given converter's {@link ObjectMapper}, or
	 * {@code null} when the converter is not a Jackson 2 HTTP message converter.
	 *
	 * @param converter the message converter
	 * @return the JSON converter, or {@code null}
	 */
	public static ContentConverter<String> converter(final Object converter) {
		if (converter instanceof MappingJackson2HttpMessageConverter jackson2Converter) {
			return new RequestConverter<>(jackson2Converter.getObjectMapper());
		}
		return null;
	}

	/**
	 * Hide constructor.
	 */
	private Jackson2SpringJson() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Reads and writes JSON with the {@link ObjectMapper} taken from a Jackson 2 Spring converter. The mapper is wrapped
	 * without changing it, so Spring keeps the configuration it already built.
	 *
	 * @param <T> the type of the object to which the JSON content will be converted
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static final class RequestConverter<T> extends JsonHttpContentConverter<T> {

		/**
		 * The builder that reads and writes with the Spring mapper.
		 */
		private final Jackson2JsonBuilder jsonBuilder;

		/**
		 * @param mapper the mapper that reads and writes JSON
		 */
		private RequestConverter(final ObjectMapper mapper) {
			this.jsonBuilder = new Jackson2JsonBuilder(mapper, false);
		}

		/**
		 * @see ContentConverter#from(Object, ApiMimeType, Class)
		 * @see JsonBuilder#fromJsonObject(Object, Class)
		 */
		@Override
		public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
			return from(obj, o -> jsonBuilder.fromJsonObject(o, targetClass), targetClass::getName);
		}

		/**
		 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
		 * @see JsonBuilder#fromJsonObject(Object, GenericClass)
		 */
		@Override
		public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
			return from(obj, o -> jsonBuilder.fromJsonObject(o, targetGenericClass), () -> targetGenericClass.getType().getTypeName());
		}

		/**
		 * @see ContentConverter#toString(Object, ApiMimeType)
		 * @see Jackson2JsonBuilder#toJsonString(Object)
		 */
		@Override
		public String toString(final Object body, final ApiMimeType mimeType) {
			return jsonBuilder.toJsonString(body);
		}
	}
}
