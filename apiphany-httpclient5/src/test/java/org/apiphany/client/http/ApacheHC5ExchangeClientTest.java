package org.apiphany.client.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.Closeable;
import java.util.Queue;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Fields;

/**
 * Test class for {@link ApacheHC5HttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class ApacheHC5ExchangeClientTest {

	@SuppressWarnings("resource")
	@Test
	void shouldBuildAndCloseTheHttpClient() throws Exception {
		CloseableHttpClient httpClient = null;
		Queue<Closeable> closeables = null;

		try (ApacheHC5HttpExchangeClient client = new ApacheHC5HttpExchangeClient()) {
			httpClient = client.getHttpClient();
			closeables = Fields.IgnoreAccess.get(httpClient, "closeables");

			assertNotNull(client);
			assertNotNull(httpClient);

			assertNotNull(closeables);
			assertThat(closeables, hasSize(1));
		}

		assertNotNull(closeables);
		assertThat(closeables, hasSize(0));
	}

}
