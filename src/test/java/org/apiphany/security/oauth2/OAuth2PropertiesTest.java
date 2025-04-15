package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * Test class for {@link OAuth2Properties}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2PropertiesTest {

	@Test
	void shouldLoadFromFile() throws JsonProcessingException {
		String json = Strings.fromFile("/oauth2-properties.json");

		final ObjectMapper propertiesObjectMapper = new ObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

		OAuth2Properties result = propertiesObjectMapper.readValue(json, OAuth2Properties.class);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldSerializeToJson() throws JsonProcessingException {
		String json = Strings.fromFile("/oauth2-properties.json");

		final ObjectMapper propertiesObjectMapper = new ObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

		OAuth2Properties result = propertiesObjectMapper.readValue(json, OAuth2Properties.class);

		json = result.toString();

		final ObjectMapper objectMapper = new ObjectMapper();

		result = objectMapper.readValue(json, OAuth2Properties.class);

		assertThat(result, notNullValue());
	}
}
