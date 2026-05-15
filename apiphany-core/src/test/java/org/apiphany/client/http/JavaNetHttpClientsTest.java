package org.apiphany.client.http;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link JavaNetHttpClients}.
 *
 * @author Radu Sebastian LAZIN
 */
class JavaNetHttpClientsTest {

	@Nested
	class GetUsableTimeoutTests {

		@Test
		void shouldReturnANullUsableTimeputFromNull() {
			Duration timeout = JavaNetHttpClients.getTimeout(null, t -> null);

			assertThat(timeout, equalTo(null));
		}

		@Test
		void shouldReturnGivenTimeoutWhenUsableAndNotInfinite() {
			Duration timeout = JavaNetHttpClients.getTimeout(null, t -> Duration.ofSeconds(10));

			assertThat(timeout, equalTo(Duration.ofSeconds(10)));
		}
	}
}
