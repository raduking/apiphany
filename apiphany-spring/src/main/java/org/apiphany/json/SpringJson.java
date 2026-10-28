package org.apiphany.json;

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apiphany.client.ContentConverter;
import org.apiphany.json.jackson2.Jackson2Library;
import org.apiphany.json.jackson2.Jackson2SpringJson;
import org.apiphany.json.jackson3.Jackson3Library;
import org.apiphany.json.jackson3.Jackson3SpringJson;
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
 * A JSON-capable {@link HttpMessageConverter} may expose a mapper configured by Spring (for example, from the
 * application's Jackson configuration). When present, that converter is used by the Spring-based exchange clients to
 * write JSON request bodies. If no usable JSON converter is found, the exchange client falls back to
 * {@link JsonBuilder}.
 * <p>
 * This class resolves Jackson 2 and Jackson 3 converters. It verifies each library's presence before delegating to the
 * corresponding resolver, allowing the library-specific code to reference Jackson types directly without reflective
 * class loading.
 *
 * @author Radu Sebastian LAZIN
 */
public final class SpringJson {

	/**
	 * Field name holding the message converters configured on a Spring client builder.
	 */
	private static final String MESSAGE_CONVERTERS_FIELD_NAME = "messageConverters";

	/**
	 * Field name holding the converters configurer on a {@link RestClient.Builder}.
	 */
	private static final String CONVERTERS_CONFIGURER_FIELD_NAME = "convertersConfigurer";

	/**
	 * Field name holding the JSON converter on a {@link HttpMessageConverters.ClientBuilder}.
	 */
	private static final String JSON_CONVERTER_FIELD_NAME = "jsonConverter";

	/**
	 * Maps each optional JSON library to the function that resolves a Spring message converter to a
	 * {@link ContentConverter}. A resolver is used only when its library is present.
	 */
	private static final Map<OptionalLibrary<?>, Function<HttpMessageConverter<?>, ContentConverter<?>>> CONVERTER_RESOLVERS_MAP = Map.of(
			Jackson2Library.DESCRIPTOR, Jackson2SpringJson::converter,
			Jackson3Library.DESCRIPTOR, Jackson3SpringJson::converter);

	/**
	 * Returns the JSON request converter configured on the given {@link RestClient.Builder}, or {@code null} when the
	 * builder has no usable JSON converter.
	 * <p>
	 * This method supports both the traditional approach (where message converters are directly set on the builder via
	 * {@code messageConverters(...)}) and the new Spring Framework 7 approach (where converters are configured via
	 * {@link HttpMessageConverters.ClientBuilder} through a configurer).
	 *
	 * @param <T> the content type the converter knows to convert
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
	 * Returns the resolved content converter from the given Spring message converter, or {@code null} if the converter is
	 * not a supported JSON converter.
	 *
	 * @param <T> the content type the converter knows to convert
	 * @param messageConverter any Spring message converter
	 * @return the resolved content converter, or {@code null}
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
	 * Returns {@code true} when the converter supports JSON media types.
	 * <p>
	 * A converter is considered to support JSON if any of its supported media types has subtype {@code json} or ends with
	 * {@code +json} (case-insensitive).
	 *
	 * @param converter the message converter
	 * @return {@code true} when the converter supports JSON, {@code false} otherwise
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
				b -> Fields.IgnoreAccess.get(b, MESSAGE_CONVERTERS_FIELD_NAME));
	}

	/**
	 * Returns the converters configurer set on the builder.
	 *
	 * @param restClientBuilder the REST client builder
	 * @return the converters configurer, or {@code null}
	 */
	private static Consumer<HttpMessageConverters.ClientBuilder> convertersConfigurer(final RestClient.Builder restClientBuilder) {
		return Nullables.<RestClient.Builder, Consumer<HttpMessageConverters.ClientBuilder>>whenNotNull(restClientBuilder,
				b -> Fields.IgnoreAccess.get(b, CONVERTERS_CONFIGURER_FIELD_NAME));
	}

	/**
	 * Returns the JSON Spring message converter from the given client builder.
	 *
	 * @param <T> the converter type
	 * @param clientBuilder the client builder
	 * @return the JSON Spring message converter, or {@code null}
	 */
	private static <T> HttpMessageConverter<T> jsonConverter(final HttpMessageConverters.ClientBuilder clientBuilder) {
		return Nullables.<HttpMessageConverters.ClientBuilder, HttpMessageConverter<T>>whenNotNull(clientBuilder,
				b -> Fields.IgnoreAccess.get(b, JSON_CONVERTER_FIELD_NAME));
	}

	/**
	 * Hide constructor.
	 */
	private SpringJson() {
		throw Constructors.unsupportedOperationException();
	}
}
