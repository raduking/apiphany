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
 * Test class for {@link FieldNameSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class FieldNameSerializerTest {

	@Test
	@SuppressWarnings("resource")
	void shouldWriteFieldNameUsingConversionFunction() throws IOException {
		FieldNameSerializer<Locale> victim = new FieldNameSerializer<>(Locale::toLanguageTag);

		JsonGenerator gen = mock(JsonGenerator.class);
		SerializerProvider providers = mock(SerializerProvider.class);

		victim.serialize(Locale.US, gen, providers);

		verify(gen).writeFieldName("en-US");
		verifyNoMoreInteractions(gen);
		verifyNoMoreInteractions(providers);
	}
}
