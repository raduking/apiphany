package org.apiphany.security.ssl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
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
		String json = Strings.fromFile("/security/ssl/ssl-properties.json");

		SSLProperties result1 = JsonBuilder.fromJson(json, SSLProperties.class);

		json = result1.toString();

		SSLProperties result2 = JsonBuilder.fromJson(json, SSLProperties.class);

		assertThat(result1.toString(), equalTo(result2.toString()));
	}

}
