package org.apiphany.http;

import java.lang.reflect.Method;
import java.util.Locale;

import org.apiphany.ApiMimeType;
import org.apiphany.client.ContentConverter;
import org.apiphany.header.HeaderValues;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.morphix.lang.Messages;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.Methods;
import org.morphix.reflection.ReflectionException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestClient;

/**
 * Resolves the JSON request converter configured on a Spring HTTP client builder.
 * <p>
 * A {@code MappingJackson2HttpMessageConverter} contributes the {@code ObjectMapper} Spring built from the
 * application's Jackson configuration. That mapper writes JSON request bodies as-is. A builder with no such converter
 * has no converter, and the exchange client falls back to {@link JsonBuilder}.
 * <p>
 * Spring Boot 4's {@code JsonMapper} converter is not resolved yet.
 *
 * @author Radu Sebastian LAZIN
 */
public final class SpringJson {

	/**
	 * Jackson 2 HTTP message converter class name. Kept as a string so this class can load when that converter is absent.
	 */
	private static final String JACKSON_2_HTTP_MESSAGE_CONVERTER = "org.springframework.http.converter.json.MappingJackson2HttpMessageConverter";

	/**
	 * Field that holds the converters configured on a Spring client builder.
	 */
	private static final String MESSAGE_CONVERTERS_FIELD_NAME = "messageConverters";

	/**
	 * Method that converts the JSON to string.
	 */
	private static final String WRITE_VALUE_AS_STRING_METHOD_NAME = "writeValueAsString";

	/**
	 * Method name that returns the mapper.
	 */
	private static final String GET_OBJECT_MAPPER_METHOD_NAME = "getObjectMapper";

	/**
	 * Returns the JSON request converter configured on the given {@link RestClient.Builder}, or {@code null} when the
	 * builder has no Jackson converter.
	 *
	 * @param restClientBuilder the REST client builder
	 * @return the JSON request converter, or {@code null}
	 */
	public static ContentConverter<String> converter(final RestClient.Builder restClientBuilder) {
		return converter(messageConverters(restClientBuilder));
	}

	/**
	 * Returns the JSON request converter configured on the given {@link RestTemplateBuilder}, or {@code null} when the
	 * builder has no Jackson converter.
	 *
	 * @param restTemplateBuilder the REST template builder
	 * @return the JSON request converter, or {@code null}
	 */
	public static ContentConverter<String> converter(final RestTemplateBuilder restTemplateBuilder) {
		return converter(messageConverters(restTemplateBuilder));
	}

	/**
	 * Returns the JSON request converter from the given message converters, or {@code null} when none of them writes JSON
	 * with a configured mapper.
	 *
	 * @param messageConverters the message converters
	 * @return the JSON request converter, or {@code null}
	 */
	static ContentConverter<String> converter(final Object messageConverters) {
		if (!(messageConverters instanceof Iterable<?> converters)) {
			return null;
		}
		for (Object converter : converters) {
			if (isJackson2HttpMessageConverter(converter) && supportsJson(converter)) {
				Object mapper = mapper(converter);
				if (null != mapper) {
					return new RequestConverter(mapper);
				}
			}
		}
		return null;
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
		Class<?> type = converter.getClass();
		while (null != type && Object.class != type) {
			if (JACKSON_2_HTTP_MESSAGE_CONVERTER.equals(type.getName())) {
				return true;
			}
			type = type.getSuperclass();
		}
		return false;
	}

	/**
	 * Returns the converters already set on the builder. A builder that has not been given converters keeps a {@code null}
	 * field, and this method does not initialize the builder's defaults.
	 *
	 * @param builder the Spring client builder
	 * @return the configured converters, or {@code null}
	 */
	private static Object messageConverters(final Object builder) {
		if (null == builder) {
			return null;
		}
		return Fields.IgnoreAccess.get(builder, MESSAGE_CONVERTERS_FIELD_NAME);
	}

	/**
	 * Returns {@code true} when the converter writes JSON.
	 *
	 * @param converter the message converter
	 * @return {@code true} when one of the supported media types is JSON
	 */
	private static boolean supportsJson(final Object converter) {
		if (!(converter instanceof HttpMessageConverter<?> httpMessageConverter)) {
			return false;
		}
		for (MediaType mediaType : httpMessageConverter.getSupportedMediaTypes()) {
			String subtype = mediaType.getSubtype(); // cannot be null
			if ("json".equalsIgnoreCase(subtype) || subtype.toLowerCase(Locale.ROOT).endsWith("+json")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the mapper of the given converter.
	 *
	 * @param converter the Jackson HTTP message converter
	 * @return the mapper
	 */
	private static Object mapper(final Object converter) {
		try {
			Method method = converter.getClass().getMethod(GET_OBJECT_MAPPER_METHOD_NAME);
			return method.invoke(converter);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Hide constructor.
	 */
	private SpringJson() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Writes JSON request bodies with the {@code ObjectMapper} taken from a Spring Jackson converter. This converter does
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
			if (null == method) {
				throw new IllegalStateException(Messages.message("Method {}.{} not found!", mapperClass, WRITE_VALUE_AS_STRING_METHOD_NAME));
			}
			try {
				return Methods.invoke(method, mapper, value);
			} catch (ReflectionException e) {
				throw new IllegalStateException("Error invoking " + method, e);
			}
		}

		/**
		 * Returns the exception thrown when this converter is asked to read a response.
		 *
		 * @return the unsupported operation
		 */
		private static UnsupportedOperationException unsupported() {
			return new UnsupportedOperationException("Spring JSON converter writes JSON request bodies");
		}
	}
}
