package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.hc.core5.util.Timeout;
import org.apiphany.json.jackson2.ApiphanyHC5Jackson2Module;
import org.apiphany.json.jackson2.Jackson2JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Test class for {@link ApacheHC5Properties}.
 */
class ApacheHC5PropertiesTest {

	static class ApplicationProperties {

		private ApacheHC5Properties httpClient5;

		public ApplicationProperties() {
			// Default constructor for Jackson
		}

		public ApacheHC5Properties getHttpClient5() {
			return httpClient5;
		}

		public void setHttpClient5(final ApacheHC5Properties httpClient5) {
			this.httpClient5 = httpClient5;
		}
	}

	@Test
	void shouldLoadPropertiesFromFile() {
		String filePath = "apache-hc5-properties.yaml";
		String yaml = Strings.fromFile(filePath);

		Jackson2JsonBuilder jsonBuilder = new Jackson2JsonBuilder(new YAMLFactory());
		jsonBuilder.getObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		jsonBuilder.getObjectMapper().registerModule(ApiphanyHC5Jackson2Module.instance());

		ApplicationProperties properties = jsonBuilder.fromJsonString(yaml, ApplicationProperties.class);

		ApacheHC5Properties httpClient5Properties = properties.getHttpClient5();

		assertThat(httpClient5Properties.getConnection().getTimeToLive(), equalTo(Timeout.ofSeconds(30)));
	}
}
