package org.apiphany.json.jackson3.serializers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;

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
	void shouldDeserializeSimpleDurationInSeconds() throws JacksonException {
		assertThat(deserialize("\"1s\""), equalTo(Timeout.ofSeconds(1)));
	}

	@Test
	void shouldDeserializeSimpleDurationInMinutes() throws JacksonException {
		assertThat(deserialize("\"2m\""), equalTo(Timeout.ofMinutes(2)));
	}

	@Test
	void shouldDeserializeSimpleDurationInHours() throws JacksonException {
		assertThat(deserialize("\"3h\""), equalTo(Timeout.ofHours(3)));
	}

	@Test
	void shouldDeserializeSimpleDurationInDays() throws JacksonException {
		assertThat(deserialize("\"4d\""), equalTo(Timeout.ofDays(4)));
	}

	@Test
	void shouldDeserializeInfiniteTimeout() throws JacksonException {
		assertThat(deserialize("\"infinite\""), equalTo(Timeout.INFINITE));
	}

	@Test
	void shouldDeserializeDisabledTimeout() throws JacksonException {
		assertThat(deserialize("\"disabled\""), equalTo(Timeout.DISABLED));
	}

	@Test
	void shouldDeserializeEmptyValueToNull() throws JacksonException {
		assertThat(deserialize("\"\""), nullValue());
	}

	@Test
	void shouldDeserializeNullValueToNull() throws JacksonException {
		assertThat(deserialize("null"), nullValue());
	}

	@Test
	void shouldFallBackToApacheTimeoutParseForOtherFormats() throws JacksonException {
		assertThat(deserialize("\"10 seconds\""), equalTo(Timeout.ofSeconds(10)));
	}

	@Test
	void shouldThrowExceptionWhenTimeoutFormatIsInvalid() {
		DatabindException e = assertThrows(DatabindException.class, () -> deserialize("\"" + NOT_A_TIMEOUT + "\""));

		assertThat(e.getMessage(), startsWith("Invalid timeout format: " + NOT_A_TIMEOUT
				+ ", Expected format for <Long><SPACE><java.util.concurrent.TimeUnit>: " + NOT_A_TIMEOUT));
	}

	private Timeout deserialize(final String json) throws JacksonException {
		try (JsonParser parser = MAPPER.createParser(json)) {
			parser.nextToken();
			return deserializer.deserialize(parser, null);
		}
	}
}
