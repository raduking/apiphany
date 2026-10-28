package org.apiphany.json.jackson3;

import org.apiphany.client.ContentConverter;
import org.apiphany.json.SpringJson;
import org.morphix.reflection.Constructors;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import tools.jackson.databind.json.JsonMapper;

/**
 * Resolves a {@link JacksonJsonHttpMessageConverter} to a {@link ContentConverter} that reads and writes JSON with that
 * converter's {@link JsonMapper}.
 * <p>
 * This class uses Jackson 3 and Spring's Jackson 3 types directly. {@link SpringJson} only calls
 * {@link #converter(HttpMessageConverter)} after verifying {@link Jackson3Library#isPresent()}, so this class is never
 * loaded unless Jackson 3 is on the classpath.
 * <p>
 * Spring Framework 7 uses {@link JacksonJsonHttpMessageConverter} for Jackson 3; this resolver extracts the mapper via
 * {@link JacksonJsonHttpMessageConverter#getMapper()}.
 *
 * @author Radu Sebastian LAZIN
 */
public final class Jackson3SpringJson {

	/**
	 * Returns a {@link ContentConverter} that reads and writes JSON with the given converter's {@link JsonMapper}, or
	 * {@code null} when the converter is not a {@link JacksonJsonHttpMessageConverter}.
	 *
	 * @param <T> the content type the converter knows to convert
	 * @param messageConverter the Spring message converter
	 * @return the JSON converter, or {@code null}
	 */
	public static <T> ContentConverter<T> converter(final HttpMessageConverter<?> messageConverter) {
		if (messageConverter instanceof JacksonJsonHttpMessageConverter jacksonConverter) {
			return new Jackson3JsonHttpContentConverter<>(jacksonConverter.getMapper());
		}
		return null;
	}

	/**
	 * Hide constructor.
	 */
	private Jackson3SpringJson() {
		throw Constructors.unsupportedOperationException();
	}

}
