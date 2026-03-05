package org.apiphany.json.jackson3.serializers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apiphany.ApiMethod;
import org.apiphany.RequestMethod;
import org.apiphany.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.core.JsonParser;

/**
 * Test class for {@link RequestMethodDeserializer}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class RequestMethodDeserializerTest {

	private static final String SOME_REQUEST_METHOD = "SOME_REQUEST_METHOD";

	private final RequestMethodDeserializer victim = new RequestMethodDeserializer();

	@ParameterizedTest
	@EnumSource(HttpMethod.class)
	@SuppressWarnings("resource")
	void shouldSerializeValidRequestMethod(final HttpMethod method) {
		JsonParser parser = mock(JsonParser.class);
		doReturn(method.toString()).when(parser).getValueAsString();

		RequestMethod result = victim.deserialize(parser, null);

		assertThat(result, equalTo(method));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSerializeInvalidRequestMethod() {
		JsonParser parser = mock(JsonParser.class);
		doReturn(SOME_REQUEST_METHOD).when(parser).getValueAsString();

		RequestMethod result = victim.deserialize(parser, null);

		assertThat(result, equalTo(ApiMethod.of(SOME_REQUEST_METHOD)));
	}
}
