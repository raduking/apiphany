package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apiphany.json.jackson2.Jackson2JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Test class for {@link OAuth2Properties}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2PropertiesTest {

	@Test
	void shouldLoadFromFile() throws JsonProcessingException {
		String json = Strings.fromFile("security/oauth2/oauth2-properties.json");

		ObjectMapper propertiesObjectMapper = new ObjectMapper()
				.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

		OAuth2Properties result = propertiesObjectMapper.readValue(json, OAuth2Properties.class);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldSerializeToJson() {
		Jackson2JsonBuilder jsonBuilder;
		String json = Strings.fromFile("security/oauth2/oauth2-properties.json");

		ObjectMapper propertiesObjectMapper = JsonMapper.builder()
				.propertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE)
				.build();
		jsonBuilder = Jackson2JsonBuilder.custom(propertiesObjectMapper);

		OAuth2Properties result1 = jsonBuilder.fromJsonString(json, OAuth2Properties.class);

		json = result1.toString();

		ObjectMapper objectMapper = JsonMapper.builder()
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
				.build();
		jsonBuilder = Jackson2JsonBuilder.custom(objectMapper);

		OAuth2Properties result2 = jsonBuilder.fromJsonString(json, OAuth2Properties.class);

		assertThat(result1.toString(), equalTo(result2.toString()));
	}

	@Test
	void shouldReturnNullOnGetProviderWhenProvidersMapIsEmpty() {
		OAuth2Properties props = OAuth2Properties.of();

		OAuth2ProviderDetails result = props.getProviderDetails("test");

		assertNull(result);
	}

	@Test
	void shouldReturnNullOnGetRegistrationWhenRegistrationMapIsEmpty() {
		OAuth2Properties props = OAuth2Properties.of();

		OAuth2ClientRegistration result = props.getClientRegistration("test");

		assertNull(result);
	}
}
