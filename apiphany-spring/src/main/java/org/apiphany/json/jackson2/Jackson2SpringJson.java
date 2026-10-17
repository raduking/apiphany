package org.apiphany.json.jackson2;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.io.ContentType;
import org.morphix.convert.ObjectConverterException;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
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
			return new RequestConverter(jackson2Converter.getObjectMapper());
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
	private static final class RequestConverter implements ContentConverter<String> {

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
		 * @see ContentConverter#isConvertible(ApiMimeType, Object, HeaderValues)
		 */
		@Override
		public <H> boolean isConvertible(final ApiMimeType mimeType, final H headers, final HeaderValues chain) {
			if (null != mimeType) {
				return ContentType.APPLICATION_JSON == mimeType.contentType();
			}
			return false;
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
		 * @see ContentConverter#toString(Object, ApiMimeType)
		 */
		@Override
		public String toString(final Object body, final ApiMimeType mimeType) {
			try {
				return mapper.writeValueAsString(body);
			} catch (JsonProcessingException e) {
				throw new ObjectConverterException("Error converting object to JSON", e);
			}
		}

		/**
		 * Returns the exception thrown when this converter is asked to read a response.
		 *
		 * @return the unsupported operation
		 */
		private static UnsupportedOperationException unsupported() {
			return new UnsupportedOperationException("Jackson 2 Spring JSON converter writes JSON request bodies");
		}
	}
}
