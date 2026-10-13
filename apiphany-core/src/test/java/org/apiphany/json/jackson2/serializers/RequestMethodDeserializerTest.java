package org.apiphany.json.jackson2.serializers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.apiphany.ApiMethod;
import org.apiphany.RequestMethod;
import org.apiphany.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.core.JsonParser;

/**
 * Test class for {@link RequestMethodDeserializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class RequestMethodDeserializerTest {

	private static final String SOME_REQUEST_METHOD = "SOME_REQUEST_METHOD";

	private final RequestMethodDeserializer victim = new RequestMethodDeserializer();

	@ParameterizedTest
	@EnumSource(HttpMethod.class)
	@SuppressWarnings("resource")
	void shouldSerializeValidRequestMethod(final HttpMethod method) throws IOException {
		JsonParser parser = mock(JsonParser.class);
		doReturn(method.toString()).when(parser).getValueAsString();

		RequestMethod result = victim.deserialize(parser, null);

		assertThat(result, equalTo(method));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldSerializeInvalidRequestMethod() throws IOException {
		JsonParser parser = mock(JsonParser.class);
		doReturn(SOME_REQUEST_METHOD).when(parser).getValueAsString();

		RequestMethod result = victim.deserialize(parser, null);

		assertThat(result, equalTo(ApiMethod.of(SOME_REQUEST_METHOD)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNullWhenJsonValueIsNull() throws IOException {
		JsonParser parser = mock(JsonParser.class);
		doReturn(null).when(parser).getValueAsString();

		RequestMethod result = victim.deserialize(parser, null);

		assertThat(result, nullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnEmptyApiMethodWhenJsonValueIsEmpty() throws IOException {
		JsonParser parser = mock(JsonParser.class);
		doReturn("").when(parser).getValueAsString();

		RequestMethod result = victim.deserialize(parser, null);

		assertThat(result, equalTo(ApiMethod.of("")));
	}
}
