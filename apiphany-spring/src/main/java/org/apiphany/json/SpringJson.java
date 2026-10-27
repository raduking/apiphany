package org.apiphany.json;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apiphany.client.ContentConverter;
import org.apiphany.json.jackson2.Jackson2Library;
import org.apiphany.json.jackson2.Jackson2SpringJson;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.lang.collections.Iterables;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.runtime.OptionalLibrary;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.client.RestClient;

/**
 * Resolves the JSON request converter configured on a Spring HTTP client builder.
 * <p>
 * A JSON-capable {@code HttpMessageConverter} contributes the mapper Spring built from the application's JSON library
 * configuration. That mapper writes JSON request bodies as-is. A builder with no such converter has no converter, and
 * the exchange client falls back to {@link JsonBuilder}.
 * <p>
 * This class is library-agnostic. It verifies {@link Jackson2Library#isPresent()} before consulting
 * {@link Jackson2SpringJson} for each JSON-capable message converter found on the builder. That presence check is what
 * allows {@link Jackson2SpringJson} to use Jackson 2 types directly instead of reflection.
 *
 * @author Radu Sebastian LAZIN
 */
public final class SpringJson {

	/**
	 * Field that holds the converters configured on a Spring client builder.
	 */
	private static final String MESSAGE_CONVERTERS_FIELD_NAME = "messageConverters";

	private static final String CONVERTERS_CONFIGURER_FIELD_NAME = "convertersConfigurer";

	private static final String JSON_CONVERTER_FIELD_NAME = "jsonConverter";

	/**
	 * Maps each optional JSON library to the function that resolves a Spring message converter to a
	 * {@link ContentConverter}. A resolver is used only when its library is present.
	 */
	private static final Map<OptionalLibrary<?>, Function<HttpMessageConverter<?>, ContentConverter<?>>> CONVERTER_RESOLVERS_MAP = Map.of(
			Jackson2Library.DESCRIPTOR, Jackson2SpringJson::converter);

	/**
	 * Returns the JSON request converter configured on the given {@link RestClient.Builder}, or {@code null} when the
	 * builder has no usable JSON converter.
	 *
	 * @param <T> the content type the converter knows to convert
	 *
	 * @param restClientBuilder the REST client builder
	 * @return the JSON request converter, or {@code null}
	 */
	public static <T> ContentConverter<T> converter(final RestClient.Builder restClientBuilder) {
		Iterable<HttpMessageConverter<?>> messageConverters = messageConverters(restClientBuilder);
		if (null != messageConverters) {
			return converter(messageConverters);
		}
		Consumer<HttpMessageConverters.ClientBuilder> convertersConfigurer = convertersConfigurer(restClientBuilder);
		if (null == convertersConfigurer) {
			return null;
		}
		HttpMessageConverters.ClientBuilder clientBuilder = HttpMessageConverters.forClient();
		convertersConfigurer.accept(clientBuilder);
		HttpMessageConverter<?> jsonConverter = jsonConverter(clientBuilder);
		return resolveContentConverter(jsonConverter);
	}

	/**
	 * Returns the JSON request converter configured on the given {@link RestTemplateBuilder}, or {@code null} when the
	 * builder has no usable JSON converter.
	 *
	 * @param <T> the content type the converter knows to convert
	 *
	 * @param restTemplateBuilder the REST template builder
	 * @return the JSON request converter, or {@code null}
	 */
	public static <T> ContentConverter<T> converter(final RestTemplateBuilder restTemplateBuilder) {
		return converter(messageConverters(restTemplateBuilder));
	}

	/**
	 * Returns the JSON request converter from the given message converters, or {@code null} when none of them resolves to a
	 * usable JSON converter.
	 *
	 * @param <T> the content type the converter knows to convert
	 *
	 * @param messageConverters the message converters
	 * @return the JSON request converter, or {@code null}
	 */
	static <T> ContentConverter<T> converter(final Iterable<HttpMessageConverter<?>> messageConverters) {
		for (HttpMessageConverter<?> messageConverter : Iterables.safe(messageConverters)) {
			if (supportsJson(messageConverter)) {
				ContentConverter<T> resolved = resolveContentConverter(messageConverter);
				if (resolved != null) {
					return resolved;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the resolved content converter.
	 *
	 * @param <T> the content type the converter knows to convert
	 *
	 * @param messageConverter any Spring message converter
	 * @return the resolved content converter
	 */
	private static <T> ContentConverter<T> resolveContentConverter(final HttpMessageConverter<?> messageConverter) {
		for (Map.Entry<OptionalLibrary<?>, Function<HttpMessageConverter<?>, ContentConverter<?>>> entry : CONVERTER_RESOLVERS_MAP.entrySet()) {
			OptionalLibrary<?> optionalLibrary = entry.getKey();
			if (optionalLibrary.isPresent()) {
				Function<HttpMessageConverter<?>, ContentConverter<?>> resolverFunction = entry.getValue();
				ContentConverter<T> resolved = JavaObjects.cast(resolverFunction.apply(messageConverter));
				if (null != resolved) {
					return resolved;
				}
			}
		}
		return null;
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
	 * Returns the converters already set on the builder. A builder that has not been given converters keeps a {@code null}
	 * field, and this method does not initialize the builder's defaults.
	 *
	 * @param builder the Spring client builder
	 * @return the configured converters, or {@code null}
	 */
	private static Iterable<HttpMessageConverter<?>> messageConverters(final Object builder) {
		return Nullables.<Object, Iterable<HttpMessageConverter<?>>>whenNotNull(builder,
				b -> Fields.IgnoreAccess.get(builder, MESSAGE_CONVERTERS_FIELD_NAME));
	}

	/**
	 * Returns the converters configurer set on the builder.
	 *
	 * @param restClientBuilder the REST client builder
	 * @return the converters configurer
	 */
	private static Consumer<HttpMessageConverters.ClientBuilder> convertersConfigurer(final RestClient.Builder restClientBuilder) {
		return Nullables.<RestClient.Builder, Consumer<HttpMessageConverters.ClientBuilder>>whenNotNull(restClientBuilder,
				b -> Fields.IgnoreAccess.get(restClientBuilder, CONVERTERS_CONFIGURER_FIELD_NAME));
	}

	/**
	 * Returns the JSON Spring message converter.
	 *
	 * @param <T> the converter type
	 *
	 * @param clientBuilder the client builder
	 * @return the JSON Spring message converter
	 */
	private static <T> HttpMessageConverter<T> jsonConverter(final HttpMessageConverters.ClientBuilder clientBuilder) {
		return Nullables.<HttpMessageConverters.ClientBuilder, HttpMessageConverter<T>>whenNotNull(clientBuilder,
				b -> Fields.IgnoreAccess.get(clientBuilder, JSON_CONVERTER_FIELD_NAME));
	}

	/**
	 * Hide constructor.
	 */
	private SpringJson() {
		throw Constructors.unsupportedOperationException();
	}
}
