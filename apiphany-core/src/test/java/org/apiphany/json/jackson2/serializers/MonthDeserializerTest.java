package org.apiphany.json.jackson2.serializers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Month;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Test class for {@link MonthDeserializer}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class MonthDeserializerTest {

	private final MonthDeserializer victim = new MonthDeserializer();

	@ParameterizedTest
	@EnumSource(Month.class)
	@SuppressWarnings("resource")
	void shouldDeserializeMonthFromNameIgnoringCase(final Month month) throws IOException {
		JsonParser parser = mock(JsonParser.class);
		doReturn(month.name().toLowerCase(Locale.ROOT)).when(parser).getValueAsString();

		Month result = victim.deserialize(parser, null);

		assertThat(result, equalTo(month));
	}

	@ParameterizedTest
	@EnumSource(Month.class)
	@SuppressWarnings("resource")
	void shouldDeserializeMonthFromMonthNumberString(final Month month) throws IOException {
		JsonParser parser = mock(JsonParser.class);
		doReturn(String.valueOf(month.getValue())).when(parser).getValueAsString();

		Month result = victim.deserialize(parser, null);

		assertThat(result, equalTo(month));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDeserializeMonthFromMonthNumber() throws IOException {
		JsonParser parser = mock(JsonParser.class);
		doReturn(true).when(parser).hasToken(JsonToken.VALUE_NUMBER_INT);
		doReturn(3).when(parser).getIntValue();

		Month result = victim.deserialize(parser, null);

		assertThat(result, equalTo(Month.MARCH));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNullWhenValueIsMissing() throws IOException {
		JsonParser parser = mock(JsonParser.class);

		Month result = victim.deserialize(parser, null);

		assertThat(result, nullValue());
	}
}
