package org.apiphany.json.jackson2;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.client.http.JsonHttpContentConverter;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Resolves a Jackson 2 {@code MappingJackson2HttpMessageConverter} to a {@link ContentConverter} that writes JSON
 * request bodies with that converter's {@link ObjectMapper}.
 * <p>
 * This class uses Jackson 2 and Spring's Jackson 2 types directly. {@link org.apiphany.http.SpringJson} only calls
 * {@link #converter(Object)} after verifying {@code Jackson2Library.isPresent()}, so this class is never loaded unless
 * Jackson 2 is on the classpath.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Jackson2SpringJson {

	/**
	 * Returns a {@link ContentConverter} that writes JSON request bodies with the given converter's {@link ObjectMapper},
	 * or {@code null} when the converter is not a Jackson 2 HTTP message converter.
	 *
	 * @param converter the message converter
	 * @return the JSON request converter, or {@code null}
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
	 * Writes JSON request bodies with the {@link ObjectMapper} taken from a Jackson 2 Spring converter. This converter does
	 * not read responses.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static final class RequestConverter<T> extends JsonHttpContentConverter<T> {

		/**
		 * The mapper that writes the request body.
		 */
		private final ObjectMapper mapper;

		/**
		 * @param mapper the mapper that writes the request body
		 */
		private RequestConverter(final ObjectMapper mapper) {
			this.mapper = mapper;
		}

		/**
		 * @see ContentConverter#from(Object, ApiMimeType, Class)
		 */
		@Override
		public T from(final Object obj, final ApiMimeType mimeType, final Class<T> targetClass) {
			try {
				return switch (obj) {
					case String str -> mapper.readValue(str, targetClass);
					case byte[] bytes -> mapper.readValue(bytes, targetClass);
					case InputStream is -> mapper.readValue(is, targetClass);
					default -> throw converterException(targetClass.getName());
				};
			} catch (IOException e) {
				throw converterException(targetClass.getName(), e);
			}
		}

		/**
		 * @see ContentConverter#from(Object, ApiMimeType, GenericClass)
		 */
		@Override
		public T from(final Object obj, final ApiMimeType mimeType, final GenericClass<T> targetGenericClass) {
			TypeReference<T> typeReference = new TypeReference<>() {
				@Override
				public Type getType() {
					return targetGenericClass.getType();
				}
			};
			try {
				return switch (obj) {
					case String str -> mapper.readValue(str, typeReference);
					case byte[] bytes -> mapper.readValue(bytes, typeReference);
					case InputStream is -> mapper.readValue(is, typeReference);
					default -> throw converterException(targetGenericClass.getType().getTypeName());
				};
			} catch (IOException e) {
				throw converterException(targetGenericClass.getType().getTypeName(), e);
			}
		}

		/**
		 * @see ContentConverter#toString(Object, ApiMimeType)
		 */
		@Override
		public String toString(final Object body, final ApiMimeType mimeType) {
			try {
				return mapper.writeValueAsString(body);
			} catch (JsonProcessingException e) {
				throw converterException(String.class.getName(), e);
			}
		}
	}
}
