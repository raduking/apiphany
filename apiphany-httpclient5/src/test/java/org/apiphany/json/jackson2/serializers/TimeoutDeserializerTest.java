package org.apiphany.json.jackson2.serializers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for {@link TimeoutDeserializer}.
 *
 * @author Radu Sebastian LAZIN
 */
class TimeoutDeserializerTest {

	private static final String NOT_A_TIMEOUT = "not-a-timeout";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final TimeoutDeserializer deserializer = new TimeoutDeserializer();

	@Test
	void shouldDeserializeSimpleDurationInSeconds() throws IOException {
		assertThat(deserialize("\"1s\""), equalTo(Timeout.ofSeconds(1)));
	}

	@Test
	void shouldDeserializeSimpleDurationInMinutes() throws IOException {
		assertThat(deserialize("\"2m\""), equalTo(Timeout.ofMinutes(2)));
	}

	@Test
	void shouldDeserializeSimpleDurationInHours() throws IOException {
		assertThat(deserialize("\"3h\""), equalTo(Timeout.ofHours(3)));
	}

	@Test
	void shouldDeserializeSimpleDurationInDays() throws IOException {
		assertThat(deserialize("\"4d\""), equalTo(Timeout.ofDays(4)));
	}

	@Test
	void shouldDeserializeInfiniteTimeout() throws IOException {
		assertThat(deserialize("\"infinite\""), equalTo(Timeout.INFINITE));
	}

	@Test
	void shouldDeserializeDisabledTimeout() throws IOException {
		assertThat(deserialize("\"disabled\""), equalTo(Timeout.DISABLED));
	}

	@Test
	void shouldDeserializeEmptyValueToNull() throws IOException {
		assertThat(deserialize("\"\""), nullValue());
	}

	@Test
	void shouldDeserializeNullValueToNull() throws IOException {
		assertThat(deserialize("null"), nullValue());
	}

	@Test
	void shouldFallBackToApacheTimeoutParseForOtherFormats() throws IOException {
		assertThat(deserialize("\"10 seconds\""), equalTo(Timeout.ofSeconds(10)));
	}

	@Test
	void shouldThrowExceptionWhenTimeoutFormatIsInvalid() {
		IOException e = assertThrows(IOException.class, () -> deserialize("\"" + NOT_A_TIMEOUT + "\""));

		assertThat(e.getMessage(), equalTo("Invalid timeout format: " + NOT_A_TIMEOUT));
	}

	private Timeout deserialize(final String json) throws IOException {
		try (JsonParser parser = MAPPER.createParser(json)) {
			parser.nextToken();
			return deserializer.deserialize(parser, MAPPER.getDeserializationContext());
		}
	}
}
