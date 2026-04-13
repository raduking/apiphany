package org.apiphany.json.jackson2.serializers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.apiphany.security.Sensitive;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;

/**
 * Test class for {@link RedactedValueSerializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class RedactedValueSerializerTest {

	@Test
	void shouldHaveDefaultConstructor() {
		RedactedValueSerializer serializer = assertDoesNotThrow(() -> new RedactedValueSerializer()); // NOSONAR

		assertThat(serializer.getType(), is(nullValue()));
	}

	@Test
	void shouldHaveConstructorWithType() {
		RedactedValueSerializer serializer = assertDoesNotThrow(() -> new RedactedValueSerializer(null));

		assertThat(serializer.getType(), is(nullValue()));
	}

	@Test
	void shouldReturnGivenType() {
		JavaType type = mock(JavaType.class);
		RedactedValueSerializer serializer = new RedactedValueSerializer(type);

		assertThat(serializer.getType(), sameInstance(type));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRedactNullValue() throws IOException {
		RedactedValueSerializer serializer = new RedactedValueSerializer();

		JsonGenerator gen = mock(JsonGenerator.class);

		serializer.serialize(null, gen, null);

		verify(gen).writeNull();
		verifyNoMoreInteractions(gen);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRedactValueWithoutType() throws IOException {
		RedactedValueSerializer serializer = new RedactedValueSerializer();

		JsonGenerator gen = mock(JsonGenerator.class);

		serializer.serialize("sensitive value", gen, null);

		verify(gen).writeString(Sensitive.Value.REDACTED);
		verifyNoMoreInteractions(gen);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRedactCollectionLikeValue() throws IOException {
		JavaType type = mock(JavaType.class);
		doReturn(true).when(type).isCollectionLikeType();
		RedactedValueSerializer serializer = new RedactedValueSerializer(type);

		JsonGenerator gen = mock(JsonGenerator.class);

		serializer.serialize("sensitive value", gen, null);

		verify(gen).writeStartArray();
		verify(gen).writeString(Sensitive.Value.REDACTED);
		verify(gen).writeEndArray();
		verifyNoMoreInteractions(gen);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRedactArrayValue() throws IOException {
		JavaType type = mock(JavaType.class);
		doReturn(true).when(type).isArrayType();
		RedactedValueSerializer serializer = new RedactedValueSerializer(type);

		JsonGenerator gen = mock(JsonGenerator.class);

		serializer.serialize("sensitive value", gen, null);

		verify(gen).writeStartArray();
		verify(gen).writeString(Sensitive.Value.REDACTED);
		verify(gen).writeEndArray();
		verifyNoMoreInteractions(gen);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRedactMapLikeValue() throws IOException {
		JavaType type = mock(JavaType.class);
		doReturn(true).when(type).isMapLikeType();
		RedactedValueSerializer serializer = new RedactedValueSerializer(type);

		JsonGenerator gen = mock(JsonGenerator.class);

		serializer.serialize("sensitive value", gen, null);

		verify(gen).writeStartObject();
		verify(gen).writeStringField(Sensitive.Field.SERIALIZED_NAME, Sensitive.Value.REDACTED);
		verify(gen).writeEndObject();
		verifyNoMoreInteractions(gen);
	}

	@Test
	@SuppressWarnings("resource")
	void shouldRedactValueWithType() throws IOException {
		JavaType type = mock(JavaType.class);
		RedactedValueSerializer serializer = new RedactedValueSerializer(type);

		JsonGenerator gen = mock(JsonGenerator.class);

		serializer.serialize("sensitive value", gen, null);

		verify(gen).writeString(Sensitive.Value.REDACTED);
		verifyNoMoreInteractions(gen);
	}

	@Test
	void shouldReturnSameInstanceWhenContextualizingWithoutProperty() {
		RedactedValueSerializer serializer = new RedactedValueSerializer();

		JsonSerializer<?> contextualizedSerializer = assertDoesNotThrow(() -> serializer.createContextual(null, null));

		assertThat(contextualizedSerializer, sameInstance(serializer));
	}

	@Test
	void shouldReturnNewInstanceWhenContextualizingWithProperty() {
		RedactedValueSerializer serializer = new RedactedValueSerializer();

		BeanProperty property = mock(BeanProperty.class);
		JavaType type = mock(JavaType.class);
		doReturn(type).when(property).getType();

		RedactedValueSerializer contextualizedSerializer = (RedactedValueSerializer) serializer.createContextual(null, property);

		assertThat(contextualizedSerializer.getType(), sameInstance(type));
	}
}
