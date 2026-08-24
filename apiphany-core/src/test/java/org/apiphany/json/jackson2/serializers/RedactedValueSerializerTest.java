package org.apiphany.json.jackson2.serializers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.List;

import org.apiphany.json.jackson2.Jackson2JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.Sensitive;
import org.junit.jupiter.api.Nested;
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

	@Nested
	class SerializeComplexObjectTests {

		static class CardDto {

			private String number;
			private String expiry;
			private int cvv;

			CardDto() {
				// empty
			}

			CardDto(final String number, final String expiry) {
				this.number = number;
				this.expiry = expiry;
			}

			public String getNumber() {
				return number;
			}

			public void setNumber(final String number) {
				this.number = number;
			}

			public String getExpiry() {
				return expiry;
			}

			public void setExpiry(final String expiry) {
				this.expiry = expiry;
			}

			public int getCvv() {
				return cvv;
			}

			public void setCvv(final int cvv) {
				this.cvv = cvv;
			}
		}

		static class WalletDto {

			@Sensitive(visibility = Sensitive.Visibility.REDACTED)
			private CardDto card;

			WalletDto() {
				// empty
			}

			WalletDto(final CardDto card) {
				this.card = card;
			}

			public CardDto getCard() {
				return card;
			}

			public void setCard(final CardDto card) {
				this.card = card;
			}
		}

		static class HistoryDto {

			@Sensitive(visibility = Sensitive.Visibility.REDACTED)
			private CardDto card;
			private List<String> tags;

			HistoryDto(final CardDto card, final List<String> tags) {
				this.card = card;
				this.tags = tags;
			}

			public CardDto getCard() {
				return card;
			}

			public void setCard(final CardDto card) {
				this.card = card;
			}

			public List<String> getTags() {
				return tags;
			}

			public void setTags(final List<String> tags) {
				this.tags = tags;
			}
		}

		@Test
		void shouldRedactComplexObjectFieldsRecursively() {
			WalletDto dto = new WalletDto(new CardDto("4111111111111111", "12/25"));

			String json = Strings.removeAllWhitespace(Jackson2JsonBuilder.toJson(dto));

			assertThat(json, equalTo("{\"card\":{}}"));
		}

		@Test
		void shouldBeDeserializableAfterRedaction() {
			WalletDto dto = new WalletDto(new CardDto("4111111111111111", "12/25"));

			String json = Jackson2JsonBuilder.toJson(dto);
			WalletDto result = Jackson2JsonBuilder.fromJson(json, WalletDto.class);

			assertThat(result.getCard(), is(notNullValue()));
			assertThat(result.getCard().getNumber(), is(nullValue()));
			assertThat(result.getCard().getExpiry(), is(nullValue()));
			assertThat(result.getCard().getCvv(), equalTo(0));
		}

		@Test
		void shouldKeepNonAnnotatedFieldsUntouchedWhileRedactingAnnotatedObjectField() {
			HistoryDto dto = new HistoryDto(new CardDto("4111", "12/25"), List.of("a", "b"));

			String json = Strings.removeAllWhitespace(Jackson2JsonBuilder.toJson(dto));

			assertThat(json, equalTo("{\"card\":{},\"tags\":[\"a\",\"b\"]}"));
		}
	}
}
