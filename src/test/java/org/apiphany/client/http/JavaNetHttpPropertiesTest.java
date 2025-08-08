package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaNetHttpProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpPropertiesTest {

	private static final String ROOT = "java-net-http";

	@Test
	void shouldHaveSSLAsRootProperty() {
		assertThat(JavaNetHttpProperties.ROOT, equalTo(ROOT));
	}

}
