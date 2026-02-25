package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.util.Timeout;
import org.apiphany.json.JsonBuilder;
import org.apiphany.json.jackson2.ApiphanyHC5Jackson2Module;
import org.apiphany.json.jackson2.Jackson2JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Test class for {@link ApacheHC5Properties}.
 */
class ApacheHC5PropertiesTest {

	private static Jackson2JsonBuilder jsonBuilder;

	@BeforeAll
	static void setup() {
		jsonBuilder = new Jackson2JsonBuilder(new YAMLFactory());
		jsonBuilder.getObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
		jsonBuilder.getObjectMapper().registerModule(ApiphanyHC5Jackson2Module.instance());
	}

	private static <T> T fromYaml(final String yaml, final Class<T> valueType) {
		return JsonBuilder.with(jsonBuilder, () -> JsonBuilder.fromJson(yaml, valueType));
	}

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
	void shouldLoadPropertiesFromFile() throws ParseException {
		String filePath = "apache-hc5-properties.yaml";
		String yaml = Strings.fromFile(filePath);

		ApplicationProperties properties = fromYaml(yaml, ApplicationProperties.class);

		ApacheHC5Properties httpClient5Properties = properties.getHttpClient5();

		assertThat(httpClient5Properties.getConnection().getTimeToLive(), equalTo(Timeout.ofSeconds(30)));
		assertThat(httpClient5Properties.getConnection().getMaxTotal(), equalTo(100));
		assertThat(httpClient5Properties.getConnection().getMaxPerRoute(), equalTo(50));

		assertThat(httpClient5Properties.getRequest().getHttpProtocolVersion(), equalTo(ProtocolVersion.parse("HTTP/1.1")));
		assertThat(httpClient5Properties.getRequest().isProtocolUpgradeEnabled(), equalTo(true));

		assertThat(httpClient5Properties.getConnectionRequest().getTimeout(), equalTo(Timeout.INFINITE));

		assertThat(httpClient5Properties.getConnect().getTimeout(), equalTo(Timeout.ofMinutes(1)));

		assertThat(httpClient5Properties.getSocket().getTimeout(), equalTo(Timeout.ofHours(1)));
	}
}
