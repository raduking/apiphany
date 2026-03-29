package org.apiphany;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apiphany.client.ClientLifecycle;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.lang.Holder;
import org.apiphany.security.AuthenticationType;
import org.junit.jupiter.api.Test;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.resource.ScopedResource;

/**
 * Test class for {@link ApiClient} close behavior.
 *
 * @author Radu Sebastian LAZIN
 */
class ApiClientCloseTest {

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

	static class NotClosingExchangeClient extends SomeExchangeClient {

		public NotClosingExchangeClient(final ClientProperties clientProperties) {
			super(clientProperties);
		}

		@Override
		public void close() {
			super.close();
			throw new RuntimeException("Failed to close exchange client: " + getName());
		}
	}

	static class SomeHttpExchangeClient extends SomeExchangeClient implements HttpExchangeClient {

		public SomeHttpExchangeClient(final ClientProperties clientProperties) {
			super(clientProperties);
		}
	}

	static class NotClosingHttpExchangeClient extends NotClosingExchangeClient implements HttpExchangeClient {

		public NotClosingHttpExchangeClient(final ClientProperties clientProperties) {
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
	void shouldCallCloseOnExchangeClientWhenApiClientIsInitializedWithScopedResourceList() throws Exception {
		SomeExchangeClient exchangeClient = new SomeExchangeClient(clientProperties);

		ApiClient api = new ApiClient(List.of(ScopedResource.managed(exchangeClient)));

		assertFalse(exchangeClient.isClosed());

		api.close();

		assertTrue(exchangeClient.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnManagedExchangeClientsEvenIfConstructingApiClientFails() {
		SomeExchangeClient exchangeClient1 = new SomeExchangeClient(clientProperties);
		SomeOtherExchangeClient exchangeClient2 = new SomeOtherExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedResource1 = ScopedResource.managed(exchangeClient1);
		ScopedResource<ExchangeClient> scopedResource2 = ScopedResource.managed(exchangeClient2);
		List<ScopedResource<ExchangeClient>> resources = List.of(scopedResource1, scopedResource2);

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> new ApiClient(resources));

		assertThat(e.getMessage(), equalTo("Failed to instantiate [" + ApiClient.class.getName() + "]."
				+ " Client entry for authentication type: [" + AuthenticationType.NONE + ":" + exchangeClient1.getName() + "]"
				+ " already exists when trying to add client: [" + exchangeClient2.getName() + "]"));
		assertTrue(exchangeClient1.isClosed());
		assertTrue(exchangeClient2.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotCallCloseOnNonManagedExchangeClientsEvenIfConstructingApiClientFails() {
		SomeExchangeClient exchangeClient1 = new SomeExchangeClient(clientProperties);
		SomeOtherExchangeClient exchangeClient2 = new SomeOtherExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedResource1 = ScopedResource.unmanaged(exchangeClient1);
		ScopedResource<ExchangeClient> scopedResource2 = ScopedResource.unmanaged(exchangeClient2);
		List<ScopedResource<ExchangeClient>> resources = List.of(scopedResource1, scopedResource2);

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> new ApiClient(resources));

		assertThat(e.getMessage(), equalTo("Failed to instantiate [" + ApiClient.class.getName() + "]."
				+ " Client entry for authentication type: [" + AuthenticationType.NONE + ":" + exchangeClient1.getName() + "]"
				+ " already exists when trying to add client: [" + exchangeClient2.getName() + "]"));
		assertFalse(exchangeClient1.isClosed());
		assertFalse(exchangeClient2.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCallCloseOnAllExchangeClientsEvenIfSomeOfTheCloseMethodsThrowExceptions() {
		NotClosingExchangeClient exchangeClient1 = new NotClosingExchangeClient(clientProperties);
		SomeOtherExchangeClient exchangeClient2 = new SomeOtherExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedResource1 = ScopedResource.managed(exchangeClient1);
		ScopedResource<ExchangeClient> scopedResource2 = ScopedResource.managed(exchangeClient2);
		List<ScopedResource<ExchangeClient>> resources = List.of(scopedResource1, scopedResource2);

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> new ApiClient(resources));

		assertThat(e.getMessage(), equalTo("Failed to instantiate [" + ApiClient.class.getName() + "]."
				+ " Client entry for authentication type: [" + AuthenticationType.NONE + ":" + exchangeClient1.getName() + "]"
				+ " already exists when trying to add client: [" + exchangeClient2.getName() + "]"));
		assertTrue(exchangeClient1.isClosed());
		assertTrue(exchangeClient2.isClosed());
	}

	static class SomeCustomExchangeClientBuilder extends ExchangeClientBuilder {

		private ScopedResource<ExchangeClient> scopedClient;

		public SomeCustomExchangeClientBuilder(final ScopedResource<ExchangeClient> scopedClient) {
			this.scopedClient = scopedClient;
		}

		@Override
		public ScopedResource<ExchangeClient> build() {
			return scopedClient;
		}
	}

	@Test
	@SuppressWarnings({ "resource" })
	void shouldCloseExchangeClientWithEphemeralClient() {
		SomeHttpExchangeClient exchangeClient = new SomeHttpExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedClient = ScopedResource.managed(exchangeClient);
		SomeCustomExchangeClientBuilder builder = new SomeCustomExchangeClientBuilder(scopedClient);

		Api.http(builder)
				.get()
				.url(BASE_URL)
				.retrieve(String.class)
				.orNull();

		assertTrue(exchangeClient.isClosed());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldNotThrowExceptionOnCloseIfEphemeralIfApiClientThrowsExceptionOnClose() {
		NotClosingHttpExchangeClient exchangeClient = new NotClosingHttpExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedClient = ScopedResource.managed(exchangeClient);
		SomeCustomExchangeClientBuilder builder = new SomeCustomExchangeClientBuilder(scopedClient);

		assertDoesNotThrow(() -> Api.http(builder)
				.get()
				.url(BASE_URL)
				.retrieve(String.class)
				.orNull());

		assertTrue(exchangeClient.isClosed());
	}

	static class CustomApiClient extends ApiClient {

		private boolean closeCalled = false;
		private static final String SOME_EXCEPTION_MESSAGE = "BOOM! Some exception message";

		public CustomApiClient(final List<ScopedResource<ExchangeClient>> exchangeClients) {
			super(exchangeClients);
		}

		@Override
		public void close() throws Exception {
			closeCalled = true;
			super.close();
			throw new RuntimeException(SOME_EXCEPTION_MESSAGE);
		}

		public boolean isCloseCalled() {
			return closeCalled;
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldLogExceptionThrownByCloseOnCloseIfEphemeral() {
		SomeHttpExchangeClient exchangeClient = new SomeHttpExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedClient = ScopedResource.managed(exchangeClient);

		CustomApiClient apiClient = new CustomApiClient(List.of(scopedClient));
		apiClient.setLifecycle(ClientLifecycle.EPHEMERAL);

		apiClient.closeIfEphemeral();

		assertTrue(exchangeClient.isClosed());
		assertTrue(apiClient.isCloseCalled());
	}

	@Test
	@SuppressWarnings("resource")
	void shouldHandleExceptionThrownByCloseOnCloseIfEphemeralWithGivenHandler() {
		SomeHttpExchangeClient exchangeClient = new SomeHttpExchangeClient(clientProperties);
		ScopedResource<ExchangeClient> scopedClient = ScopedResource.managed(exchangeClient);

		CustomApiClient apiClient = new CustomApiClient(List.of(scopedClient));
		apiClient.setLifecycle(ClientLifecycle.EPHEMERAL);

		Holder<Exception> exceptionHolder = new Holder<>();

		apiClient.closeIfEphemeral(exceptionHolder::setValue);

		assertTrue(exchangeClient.isClosed());
		assertTrue(apiClient.isCloseCalled());
		assertThat(exceptionHolder.getValue().getMessage(), equalTo(CustomApiClient.SOME_EXCEPTION_MESSAGE));
	}
}
