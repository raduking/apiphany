package org.apiphany.json.jackson2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apiphany.ApiMimeType;
import org.apiphany.io.ContentType;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Jackson2JsonHttpContentConverter}.
 *
 * @author Radu Sebastian LAZIN
 */
class Jackson2JsonHttpContentConverterTest {

	private final Jackson2JsonHttpContentConverter<String> converter = new Jackson2JsonHttpContentConverter<>();

	@Test
	void shouldReturnTrueIfObjectIsConvertible() {
		ApiMimeType mimeType = ContentType.APPLICATION_JSON;

		boolean result = converter.isConvertible(null, mimeType, null, null);

		assertTrue(result);
	}

	@Test
	void shouldReturnFalseIfObjectIsNotConvertible() {
		ApiMimeType mimeType = ContentType.TEXT_PLAIN;

		boolean result = converter.isConvertible(null, mimeType, null, null);

		assertFalse(result);
	}

	@Test
	void shouldReturnFalseIfMimeTypeIsNull() {
		boolean result = converter.isConvertible(null, null, null, null);

		assertFalse(result);
	}
}
