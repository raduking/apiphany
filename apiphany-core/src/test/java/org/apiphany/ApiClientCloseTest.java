package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.lang.ScopedResource;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;

/**
 * Test class for {@link ApiClient} close behavior.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClientCloseTest {

	private static final String BASE_URL = "http://localhost";

	private final ClientProperties clientProperties = new ClientProperties();

	static class SomeExchangeClient implements ExchangeClient {

		final ClientProperties clientProperties;
		boolean closed = false;

		public SomeExchangeClient(final ClientProperties clientProperties) {
			this.clientProperties = clientProperties;
		}

		@Override
		public void close() {
			this.closed = true;
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		public boolean isClosed() {
			return closed;
		}
	}

	static class SomeOtherExchangeClient extends SomeExchangeClient {

		public SomeOtherExchangeClient(final ClientProperties clientProperties) {
			super(clientProperties);
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnManagedExchangeClients() throws Exception {
		ApiClient api = ApiClient.of(BASE_URL,
				ExchangeClient.builder()
						.client(SomeExchangeClient.class)
						.properties(clientProperties));

		api.close();

		SomeExchangeClient exchangeClient = JavaObjects.cast(api.getExchangeClient(AuthenticationType.NONE));

		assertTrue(exchangeClient.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnManagedExchangeClientsBuiltWithApiClientExchangeClientMethod() throws Exception {
		ApiClient api = ApiClient.of(BASE_URL,
				ApiClient.with(SomeExchangeClient.class)
						.properties(clientProperties));

		SomeExchangeClient exchangeClient = JavaObjects.cast(api.getExchangeClient(AuthenticationType.NONE));

		assertFalse(exchangeClient.isClosed());

		api.close();

		assertTrue(exchangeClient.isClosed());
	}

	@Test
	void shouldNotCallCloseOnNonManagedExchangeClients() throws Exception {
		SomeExchangeClient exchangeClient = new SomeExchangeClient(clientProperties);
		ApiClient api = ApiClient.of(BASE_URL, exchangeClient);

		api.close();

		try {
			assertFalse(exchangeClient.isClosed());
		} finally {
			exchangeClient.close();
		}
	}

	@Test
	void shouldNotCallCloseOnNonManagedExchangeClientsWhenNoBaseUrlProvided() throws Exception {
		SomeExchangeClient exchangeClient = new SomeExchangeClient(clientProperties);
		ApiClient api = new ApiClient(exchangeClient);

		api.close();

		try {
			assertFalse(exchangeClient.isClosed());
		} finally {
			exchangeClient.close();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnExchangeClientWhenApiClientIsInitializedWithUrlAndScopedResource() throws Exception {
		SomeExchangeClient exchangeClient = new SomeExchangeClient(clientProperties);

		ApiClient api = new ApiClient(BASE_URL, ScopedResource.managed(exchangeClient));

		assertFalse(exchangeClient.isClosed());

		api.close();

		assertTrue(exchangeClient.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnExchangeClientWhenApiClientIsInitializedWithScopedResource() throws Exception {
		SomeExchangeClient exchangeClient = new SomeExchangeClient(clientProperties);

		ApiClient api = new ApiClient(ScopedResource.managed(exchangeClient));

		assertFalse(exchangeClient.isClosed());

		api.close();

		assertTrue(exchangeClient.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnManagedExchangeClientsEvenIfConstructingApiClientFails() throws Exception {
		SomeExchangeClient exchangeClient1 = new SomeExchangeClient(clientProperties);
		SomeOtherExchangeClient exchangeClient2 = new SomeOtherExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedResource1 = ScopedResource.managed(exchangeClient1);
		ScopedResource<ExchangeClient> scopedResource2 = ScopedResource.managed(exchangeClient2);

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> new ApiClient(List.of(scopedResource1, scopedResource2)));

		assertThat(e.getMessage(), equalTo("Failed to instantiate [" + ApiClient.class.getName() + "]."
				+ " Client entry for authentication type: [" + AuthenticationType.NONE + ", " + exchangeClient1.getName() + "]"
				+ " already exists when trying to add client: [" + exchangeClient2.getName() + "]"));
		assertTrue(exchangeClient1.isClosed());
		assertTrue(exchangeClient2.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotCallCloseOnNonManagedExchangeClientsEvenIfConstructingApiClientFails() throws Exception {
		SomeExchangeClient exchangeClient1 = new SomeExchangeClient(clientProperties);
		SomeOtherExchangeClient exchangeClient2 = new SomeOtherExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedResource1 = ScopedResource.unmanaged(exchangeClient1);
		ScopedResource<ExchangeClient> scopedResource2 = ScopedResource.unmanaged(exchangeClient2);

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> new ApiClient(List.of(scopedResource1, scopedResource2)));

		assertThat(e.getMessage(), equalTo("Failed to instantiate [" + ApiClient.class.getName() + "]."
				+ " Client entry for authentication type: [" + AuthenticationType.NONE + ", " + exchangeClient1.getName() + "]"
				+ " already exists when trying to add client: [" + exchangeClient2.getName() + "]"));
		assertFalse(exchangeClient1.isClosed());
		assertFalse(exchangeClient2.isClosed());
	}
}