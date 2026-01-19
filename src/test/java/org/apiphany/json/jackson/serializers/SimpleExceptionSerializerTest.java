package org.apiphany.json.jackson.serializers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Test class for {@link SimpleExceptionSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class SimpleExceptionSerializerTest {

	private final SimpleExceptionSerializer victim = new SimpleExceptionSerializer();

	@SuppressWarnings("resource")
	@Test
	void shouldWriteExceptionClassName() throws IOException {
		JsonGenerator gen = mock(JsonGenerator.class);

		victim.serialize(new RuntimeException(), gen, null);

		verify(gen).writeString(RuntimeException.class.getCanonicalName());
	}

}
