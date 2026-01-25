package org.apiphany.security.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.lang.ScopedResource;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link SecuredExchangeClientBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class SecuredExchangeClientBuilderTest {

	public static class DummyExchangeClient implements ExchangeClient {

		@Override
		public <T, U> org.apiphany.ApiResponse<U> exchange(final org.apiphany.ApiRequest<T> apiRequest) {
			return null;
		}

		@Override
		public void close() {
			// empty
		}
	}

	@Test
	void shouldBuildExchangeClientBuilder() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();

		assertNotNull(builder);
	}

	@Test
	void shouldThrowExceptionIfClientIsNotSecuredWithAnyMechanism() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();
		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);

		assertThat(e.getMessage(), equalTo("Client not secured with any mechanism"));
	}

	@Test
	void shouldBuildExchangeClientWithOAuth2Security() {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.securedWith()
				.oAuth2();

		ScopedResource<ExchangeClient> exchangeClient = builder.build();

		assertNotNull(exchangeClient);
	}

	@Test
	void shouldThrowExceptionIfClientClassIsSetWithoutSecurityMechanism() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> builder.client(DummyExchangeClient.class));

		assertThat(e.getMessage(), equalTo("Cannot set exchange client class when securing an existing client"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldThrowExceptionIfClientIsSetWithoutSecurityMechanism() {
		SecuredExchangeClientBuilder builder = SecuredExchangeClientBuilder.create();
		DummyExchangeClient exchangeClient = new DummyExchangeClient();
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> builder.client(exchangeClient));

		assertThat(e.getMessage(), equalTo("Cannot set exchange client when securing an existing client"));
	}
}
