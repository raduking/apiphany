package org.apiphany.json.jackson2.serializers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.reflection.TypeHolder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for {@link TypeHolderDeserializer}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class TypeHolderDeserializerTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final TypeHolderDeserializer victim = new TypeHolderDeserializer();

	@SuppressWarnings("resource")
	private static JsonParser parserWithJson(final String json) throws IOException {
		JsonParser parser = mock(JsonParser.class);
		JsonNode node = MAPPER.readTree(json);
		doReturn(node).when(parser).readValueAsTree();
		return parser;
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNullWhenParserReturnsNoNode() throws IOException {
		JsonParser parser = mock(JsonParser.class);

		TypeHolder result = victim.deserialize(parser, null);

		assertThat(result, nullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNullWhenJsonValueIsNull() throws IOException {
		TypeHolder result = victim.deserialize(parserWithJson("null"), null);

		assertThat(result, nullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDeserializeTypeHolderFromClassNameString() throws IOException {
		TypeHolder result = victim.deserialize(parserWithJson("\"java.lang.String\""), null);

		assertThat(result.hasClassType(), is(true));
		assertThat(result.getClassType(), equalTo(String.class));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDeserializeTypeHolderFromClassTypeProperty() throws IOException {
		TypeHolder result = victim.deserialize(parserWithJson("{\"classType\":\"java.lang.String\"}"), null);

		assertThat(result.hasClassType(), is(true));
		assertThat(result.getClassType(), equalTo(String.class));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNoneWhenClassTypePropertyIsNull() throws IOException {
		TypeHolder result = victim.deserialize(parserWithJson("{\"classType\":null}"), null);

		assertThat(result.hasType(), is(false));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNoneWhenClassTypePropertyIsMissing() throws IOException {
		TypeHolder result = victim.deserialize(parserWithJson("{}"), null);

		assertThat(result.hasType(), is(false));
	}
}
