package org.apiphany.json.jackson3.serializers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonGenerator;

/**
 * Test class for {@link SimpleExceptionSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class SimpleExceptionSerializerTest {

	private final SimpleExceptionSerializer victim = new SimpleExceptionSerializer();

	@Test
	@SuppressWarnings("resource")
	void shouldWriteExceptionClassName() {
		JsonGenerator gen = mock(JsonGenerator.class);

		victim.serialize(new RuntimeException(), gen, null);

		verify(gen).writeString(RuntimeException.class.getCanonicalName());
	}
}
