package org.apiphany.json.jackson2.serializers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Test class for {@link FieldValueSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldValueSerializerTest {

	@Test
	@SuppressWarnings("resource")
	void shouldWriteStringUsingConversionFunction() throws IOException {
		FieldValueSerializer<Locale> victim = new FieldValueSerializer<>(Locale::toLanguageTag);

		JsonGenerator gen = mock(JsonGenerator.class);
		SerializerProvider providers = mock(SerializerProvider.class);

		victim.serialize(Locale.US, gen, providers);

		verify(gen).writeString("en-US");
		verifyNoMoreInteractions(gen);
		verifyNoMoreInteractions(providers);
	}
}
