package org.apiphany.json.jackson2.serializers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.apiphany.http.HttpMethod;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Test class for {@link RequestMethodSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class RequestMethodSerializerTest {

	@Test
	@SuppressWarnings("resource")
	void shouldSerializeRequestMethod() throws IOException {
		RequestMethodSerializer victim = new RequestMethodSerializer();

		JsonGenerator gen = mock(JsonGenerator.class);
		SerializerProvider ctxt = mock(SerializerProvider.class);

		victim.serialize(HttpMethod.GET, gen, ctxt);

		verify(gen).writeString(HttpMethod.GET.name());
		verifyNoMoreInteractions(gen);
		verifyNoMoreInteractions(ctxt);
	}
}
