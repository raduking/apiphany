package org.apiphany.json.jackson3.serializers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * Test class for {@link FieldNameSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldNameSerializerTest {

	@Test
	@SuppressWarnings("resource")
	void shouldWriteFieldNameUsingConversionFunction() throws JacksonException {
		FieldNameSerializer<Locale> victim = new FieldNameSerializer<>(Locale::toLanguageTag);

		JsonGenerator gen = mock(JsonGenerator.class);
		SerializationContext ctxt = mock(SerializationContext.class);

		victim.serialize(Locale.US, gen, ctxt);

		verify(gen).writeName("en-US");
		verifyNoMoreInteractions(gen);
		verifyNoMoreInteractions(ctxt);
	}
}
