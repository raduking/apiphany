package org.apiphany.lang.builder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.ApiClient;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link CanonicalClassNameBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class CanonicalClassNameBuilderTest {

	@Test
	void shouldReturnThisWhenBuildingAsSuffix() {
		String name = CanonicalClassNameBuilder.builder()
				.path("org")
				.path("apiphany")
				.path("ApiClient")
				.asSuffix()
				.build();

		assertThat(name, equalTo(ApiClient.class.getCanonicalName()));
	}

}
