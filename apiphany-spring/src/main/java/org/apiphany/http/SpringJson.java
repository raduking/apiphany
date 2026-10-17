package org.apiphany.http;

import java.util.Locale;

import org.apiphany.client.ContentConverter;
import org.apiphany.json.JsonBuilder;
import org.apiphany.json.jackson2.Jackson2SpringJson;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestClient;

/**
 * Resolves the JSON request converter configured on a Spring HTTP client builder.
 * <p>
 * A JSON-capable {@code HttpMessageConverter} contributes the mapper Spring built from the application's JSON library
 * configuration. That mapper writes JSON request bodies as-is. A builder with no such converter has no converter, and
 * the exchange client falls back to {@link JsonBuilder}.
 * <p>
 * This class is library-agnostic. {@link Jackson2SpringJson} resolves a Jackson 2 message converter. It is consulted
 * for each JSON-capable message converter found on the builder.
 *
 * @author Radu Sebastian LAZIN
 */
public final class SpringJson {

	/**
	 * Field that holds the converters configured on a Spring client builder.
	 */
	private static final String MESSAGE_CONVERTERS_FIELD_NAME = "messageConverters";

	/**
	 * Returns the JSON request converter configured on the given {@link RestClient.Builder}, or {@code null} when the
	 * builder has no usable JSON converter.
	 *
	 * @param restClientBuilder the REST client builder
	 * @return the JSON request converter, or {@code null}
	 */
	public static ContentConverter<String> converter(final RestClient.Builder restClientBuilder) {
		return converter(messageConverters(restClientBuilder));
	}

	/**
	 * Returns the JSON request converter configured on the given {@link RestTemplateBuilder}, or {@code null} when the
	 * builder has no usable JSON converter.
	 *
	 * @param restTemplateBuilder the REST template builder
	 * @return the JSON request converter, or {@code null}
	 */
	public static ContentConverter<String> converter(final RestTemplateBuilder restTemplateBuilder) {
		return converter(messageConverters(restTemplateBuilder));
	}

	/**
	 * Returns the JSON request converter from the given message converters, or {@code null} when none of them resolves to a
	 * usable JSON converter.
	 *
	 * @param messageConverters the message converters
	 * @return the JSON request converter, or {@code null}
	 */
	static ContentConverter<String> converter(final Object messageConverters) {
		if (!(messageConverters instanceof Iterable<?> converters)) {
			return null;
		}
		for (Object converter : converters) {
			if (!supportsJson(converter)) {
				continue;
			}
			ContentConverter<String> resolved = Jackson2SpringJson.converter(converter);
			if (null != resolved) {
				return resolved;
			}
		}
		return null;
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
	 * Hide constructor.
	 */
	private SpringJson() {
		throw Constructors.unsupportedOperationException();
	}
}
