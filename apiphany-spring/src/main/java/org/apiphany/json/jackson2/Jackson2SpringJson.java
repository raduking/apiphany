package org.apiphany.json.jackson2;

import org.apiphany.client.ContentConverter;
import org.morphix.reflection.Constructors;
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
			return new Jackson2JsonHttpContentConverter<>(jackson2Converter.getObjectMapper());
		}
		return null;
	}

	/**
	 * Hide constructor.
	 */
	private Jackson2SpringJson() {
		throw Constructors.unsupportedOperationException();
	}
}
