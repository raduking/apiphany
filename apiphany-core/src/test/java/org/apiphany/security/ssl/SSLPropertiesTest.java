package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SSLProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class SSLPropertiesTest {

	private static final String SSL = "ssl";

	@Test
	void shouldHaveSSLAsRootProperty() {
		assertThat(SSLProperties.ROOT, equalTo(SSL));
	}

	@Test
	void shouldSerializeToJson() {
		String json = Strings.fromFile("security/ssl/ssl-properties.json");

		SSLProperties result1 = JsonBuilder.fromJson(json, SSLProperties.class);

		json = result1.toString();

		SSLProperties result2 = JsonBuilder.fromJson(json, SSLProperties.class);

		assertThat(result1.toString(), equalTo(result2.toString()));
	}

	@Nested
	class IsEmptyTests {

		@Test
		void shouldReturnTrueIfPropertiesAreNull() {
			assertThat(SSLProperties.isEmpty(null), equalTo(true));
		}

		@Test
		void shouldReturnTrueIfPropertiesAreEmpty() {
			SSLProperties properties = new SSLProperties();

			assertThat(SSLProperties.isEmpty(properties), equalTo(true));
		}

		@Test
		void shouldReturnTrueEvenIfProtocolIsSet() {
			SSLProperties properties = new SSLProperties();
			properties.setProtocol(SSLProtocol.TLS_1_2);

			assertThat(SSLProperties.isEmpty(properties), equalTo(true));
		}

		@Test
		void shouldReturnFalseIfKeystoreLocationIsSet() {
			SSLProperties properties = new SSLProperties();
			properties.getKeystore().setLocation("keystore.jks");

			assertThat(SSLProperties.isEmpty(properties), equalTo(false));
		}

		@Test
		void shouldReturnFalseIfTruststoreLocationIsSet() {
			SSLProperties properties = new SSLProperties();
			properties.getTruststore().setLocation("truststore.jks");

			assertThat(SSLProperties.isEmpty(properties), equalTo(false));
		}
	}

	@Nested
	class IsNotEmptyTests {

		@Test
		void shouldReturnFalseIfPropertiesAreNull() {
			assertThat(SSLProperties.isNotEmpty(null), equalTo(false));
		}

		@Test
		void shouldReturnFalseIfPropertiesAreEmpty() {
			SSLProperties properties = new SSLProperties();

			assertThat(SSLProperties.isNotEmpty(properties), equalTo(false));
		}

		@Test
		void shouldReturnFalseEvenIfProtocolIsSet() {
			SSLProperties properties = new SSLProperties();
			properties.setProtocol(SSLProtocol.TLS_1_2);

			assertThat(SSLProperties.isNotEmpty(properties), equalTo(false));
		}

		@Test
		void shouldReturnTrueIfKeystoreLocationIsSet() {
			SSLProperties properties = new SSLProperties();
			properties.getKeystore().setLocation("keystore.jks");

			assertThat(SSLProperties.isNotEmpty(properties), equalTo(true));
		}

		@Test
		void shouldReturnTrueIfTruststoreLocationIsSet() {
			SSLProperties properties = new SSLProperties();
			properties.getTruststore().setLocation("truststore.jks");

			assertThat(SSLProperties.isNotEmpty(properties), equalTo(true));
		}
	}
}
