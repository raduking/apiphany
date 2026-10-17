package org.apiphany.json.jackson3.serializers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.morphix.reflection.TypeHolder;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Test class for {@link TypeHolderDeserializer}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class TypeHolderDeserializerTest {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private final TypeHolderDeserializer victim = new TypeHolderDeserializer();

	@SuppressWarnings("resource")
	private static JsonParser parserWithJson(final String json) throws JacksonException {
		JsonParser parser = mock(JsonParser.class);
		JsonNode node = MAPPER.readTree(json);
		doReturn(node).when(parser).readValueAsTree();
		return parser;
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNullWhenParserReturnsNoNode() throws JacksonException {
		JsonParser parser = mock(JsonParser.class);

		TypeHolder result = victim.deserialize(parser, null);

		assertThat(result, nullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNullWhenJsonValueIsNull() throws JacksonException {
		TypeHolder result = victim.deserialize(parserWithJson("null"), null);

		assertThat(result, nullValue());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDeserializeTypeHolderFromClassNameString() throws JacksonException {
		TypeHolder result = victim.deserialize(parserWithJson("\"java.lang.String\""), null);

		assertThat(result.hasClassType(), is(true));
		assertThat(result.getClassType(), equalTo(String.class));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDeserializeTypeHolderFromClassTypeProperty() throws JacksonException {
		TypeHolder result = victim.deserialize(parserWithJson("{\"classType\":\"java.lang.String\"}"), null);

		assertThat(result.hasClassType(), is(true));
		assertThat(result.getClassType(), equalTo(String.class));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNoneWhenClassTypePropertyIsNull() throws JacksonException {
		TypeHolder result = victim.deserialize(parserWithJson("{\"classType\":null}"), null);

		assertThat(result.hasType(), is(false));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNoneWhenClassTypePropertyIsMissing() throws JacksonException {
		TypeHolder result = victim.deserialize(parserWithJson("{}"), null);

		assertThat(result.hasType(), is(false));
	}
}
