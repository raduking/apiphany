package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SpringRestClientProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class SpringRestClientPropertiesTest {

	@Test
	void shouldHaveNullClientLibraryByDefault() {
		SpringRestClientProperties properties = new SpringRestClientProperties();

		assertThat(properties.getClientLibrary(), nullValue());
	}

	@Test
	void shouldSetAndGetClientLibrary() {
		SpringRestClientProperties properties = new SpringRestClientProperties();

		properties.setClientLibrary("http-client5");

		assertThat(properties.getClientLibrary(), equalTo("http-client5"));
	}

	@Test
	void shouldHaveCorrectRootConstant() {
		assertThat(SpringRestClientProperties.ROOT, equalTo("rest-client"));
	}
}
