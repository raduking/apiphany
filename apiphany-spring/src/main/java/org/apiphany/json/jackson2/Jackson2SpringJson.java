package org.apiphany.json.jackson2;

import java.lang.reflect.Method;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.io.ContentType;
import org.morphix.reflection.Classes;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.Methods;

/**
 * Resolves a Jackson 2 {@code MappingJackson2HttpMessageConverter} to a {@link ContentConverter} that writes JSON
 * request bodies with that converter's {@code ObjectMapper}.
 * <p>
 * The converter class name is kept as a string so this class can load when that Spring converter is absent. Spring HTTP
 * clients consult {@link #converter(Object)} for each JSON-capable message converter.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Jackson2SpringJson {

	/**
	 * Jackson 2 HTTP message converter class name. Kept as a string so this class can load when that converter is absent.
	 */
	private static final String JACKSON_2_HTTP_MESSAGE_CONVERTER = "org.springframework.http.converter.json.MappingJackson2HttpMessageConverter";

	/**
	 * Method that converts the JSON to string.
	 */
	private static final String WRITE_VALUE_AS_STRING_METHOD_NAME = "writeValueAsString";

	/**
	 * Method name that returns the mapper.
	 */
	private static final String GET_OBJECT_MAPPER_METHOD_NAME = "getObjectMapper";

	/**
	 * Returns a {@link ContentConverter} that writes JSON request bodies with the given converter's {@code ObjectMapper},
	 * or {@code null} when the converter is not a Jackson 2 HTTP message converter.
	 *
	 * @param converter the message converter
	 * @return the JSON request converter, or {@code null}
	 */
	public static ContentConverter<String> converter(final Object converter) {
		if (!isJackson2HttpMessageConverter(converter)) {
			return null;
		}
		Object mapper = mapper(converter);
		return null != mapper ? new RequestConverter(mapper) : null;
	}

	/**
	 * Returns {@code true} when the converter is a {@code MappingJackson2HttpMessageConverter} or a subclass. The check
	 * uses the class name so that the converter class is not loaded when it is absent.
	 *
	 * @param converter the message converter
	 * @return {@code true} when the converter is the Jackson 2 HTTP message converter
	 */
	private static boolean isJackson2HttpMessageConverter(final Object converter) {
		if (null == converter) {
			return false;
		}
		Class<?> type = Classes.getOneInHierarchy(converter.getClass(), JACKSON_2_HTTP_MESSAGE_CONVERTER);
		return null != type;
	}

	/**
	 * Returns the mapper of the given converter.
	 *
	 * @param converter the Jackson 2 HTTP message converter
	 * @return the mapper
	 */
	private static Object mapper(final Object converter) {
		Class<?> converterClass = converter.getClass();
		Method method = Methods.getOneDeclaredInHierarchy(GET_OBJECT_MAPPER_METHOD_NAME, converterClass);
		return Methods.invoke(method, converter);
	}

	/**
	 * Hide constructor.
	 */
	private Jackson2SpringJson() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Writes JSON request bodies with the {@code ObjectMapper} taken from a Jackson 2 Spring converter. This converter does
	 * not read responses.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	private static final class RequestConverter implements ContentConverter<String> {

		/**
		 * The mapper that writes the request body.
		 */
		private final Object mapper;

		/**
		 * @param mapper the mapper that writes the request body
		 */
		private RequestConverter(final Object mapper) {
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
			return write(mapper, body);
		}

		/**
		 * Writes the value with the mapper's {@code writeValueAsString}.
		 *
		 * @param mapper the mapper
		 * @param value the value to write
		 * @return the JSON string
		 */
		private static String write(final Object mapper, final Object value) {
			Class<?> mapperClass = mapper.getClass();
			Method method = Methods.getOneDeclared(WRITE_VALUE_AS_STRING_METHOD_NAME, mapperClass, Object.class);
			return Methods.invoke(method, mapper, value);
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
