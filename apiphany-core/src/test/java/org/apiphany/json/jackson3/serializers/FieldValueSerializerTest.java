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
 * Test class for {@link FieldValueSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldValueSerializerTest {

	@Test
	@SuppressWarnings("resource")
	void shouldWriteStringUsingConversionFunction() throws JacksonException {
		FieldValueSerializer<Locale> victim = new FieldValueSerializer<>(Locale::toLanguageTag);

		JsonGenerator gen = mock(JsonGenerator.class);
		SerializationContext ctxt = mock(SerializationContext.class);

		victim.serialize(Locale.US, gen, ctxt);

		verify(gen).writeString("en-US");
		verifyNoMoreInteractions(gen);
		verifyNoMoreInteractions(ctxt);
	}
}
