package org.apiphany.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ClientPropertiesTest}.
 *
 * @author Radu Sebastian LAZIN
 */
class ClientPropertiesTest {

	private static final String ROOT = "ROOT";

	@Test
	void shouldHaveSSLAsRootProperty() {
		assertThat(ClientProperties.CUSTOM_PROPERTIES_PREFIX_FIELD_NAME, equalTo(ROOT));
	}

}
