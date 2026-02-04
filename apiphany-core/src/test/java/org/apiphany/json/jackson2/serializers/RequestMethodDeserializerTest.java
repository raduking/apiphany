package org.apiphany.json.jackson2.serializers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.apiphany.ApiMethod;
import org.apiphany.RequestMethod;
import org.apiphany.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test class for {@link RequestMethodDeserializer}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class RequestMethodDeserializerTest {

	private static final String SOME_REQUEST_METHOD = "SOME_REQUEST_METHOD";

	@Mock
	private JsonParser parser;

	private final RequestMethodDeserializer victim = new RequestMethodDeserializer();

	@ParameterizedTest
	@EnumSource(HttpMethod.class)
	@SuppressWarnings("resource")
	void shouldSerializeValidRequestMethod(final HttpMethod method) throws IOException {
		ObjectCodec codec = mock(ObjectCodec.class);
		doReturn(codec).when(parser).getCodec();
		JsonNode node = mock(JsonNode.class);
		doReturn(node).when(codec).readTree(parser);
		doReturn(method.toString()).when(node).asText();

		RequestMethod result = victim.deserialize(parser, null);

		assertThat(result, equalTo(method));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSerializeInvalidRequestMethod() throws IOException {
		ObjectCodec codec = mock(ObjectCodec.class);
		doReturn(codec).when(parser).getCodec();
		JsonNode node = mock(JsonNode.class);
		doReturn(node).when(codec).readTree(parser);
		doReturn(SOME_REQUEST_METHOD).when(node).asText();

		RequestMethod result = victim.deserialize(parser, null);

		assertThat(result, equalTo(ApiMethod.of(SOME_REQUEST_METHOD)));
	}
}
