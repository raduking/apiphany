package org.apiphany.json.jackson3.serializers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apiphany.http.HttpMethod;
import org.junit.jupiter.api.Test;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * Test class for {@link RequestMethodSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class RequestMethodSerializerTest {

	@Test
	@SuppressWarnings("resource")
	void shouldSerializeRequestMethod() {
		RequestMethodSerializer victim = new RequestMethodSerializer();

		JsonGenerator gen = mock(JsonGenerator.class);
		SerializationContext ctxt = mock(SerializationContext.class);

		victim.serialize(HttpMethod.GET, gen, ctxt);

		verify(gen).writeString(HttpMethod.GET.name());
		verifyNoMoreInteractions(gen);
		verifyNoMoreInteractions(ctxt);
	}
}
