package org.apiphany.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.lang.ScopedResource;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ExchangeClientBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExchangeClientBuilderTest {

	static class DummyExchangeClient implements ExchangeClient {
		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
			return null;
		}

		@Override
		public void close() {
			// empty
		}
	}

	static class DummyDecoratingExchangeClient extends DecoratingExchangeClient {

		public DummyDecoratingExchangeClient(final ScopedResource<ExchangeClient> delegate) {
			super(delegate);
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
			return null;
		}
	}

	static class OtherDecoratingExchangeClient extends DecoratingExchangeClient {

		public OtherDecoratingExchangeClient(final ExchangeClient delegate) {
			super(delegate);
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
			return null;
		}
	}

	@Test
	void shouldNotAllowBothExchangeClientAndExchangeClientClassToBeNonNull() throws Exception {
		try (ExchangeClient exchangeClient = new DummyExchangeClient()) {
			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(ExchangeClient.class)
					.client(exchangeClient);

			IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

			assertThat(exception.getMessage(), equalTo("Cannot set both exchange client instance and exchange client class"));
		}
	}

	@Test
	void shouldNotAllowBothExchangeClientAndExchangeClientClassToBeNull() throws Exception {
		try (ExchangeClient exchangeClient = new DummyExchangeClient()) {
			ExchangeClientBuilder builder = ExchangeClientBuilder.create();

			IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

			assertThat(exception.getMessage(), equalTo("Either exchange client instance or exchange client class must be set"));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDecorateWithMultipleDecoratorsAndFirstClientIsManaged() throws Exception {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.decoratedWith(DummyDecoratingExchangeClient.class)
				.decoratedWith(DummyDecoratingExchangeClient.class);

		ScopedResource<ExchangeClient> scopedClient = null;
		ExchangeClient client = null;
		ExchangeClient innerClient = null;
		ExchangeClient innermostClient = null;
		try {
			scopedClient = builder.build();
			client = scopedClient.unwrap();
			innerClient = ((DummyDecoratingExchangeClient) client).getExchangeClient();
			innermostClient = ((DummyDecoratingExchangeClient) innerClient).getExchangeClient();
		} finally {
			if (null != scopedClient) {
				scopedClient.closeIfManaged();
			}
		}

		assertThat(client.getClass(), equalTo(DummyDecoratingExchangeClient.class));
		assertThat(innerClient.getClass(), equalTo(DummyDecoratingExchangeClient.class));
		assertThat(innermostClient.getClass(), equalTo(DummyExchangeClient.class));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDecorateWithMultipleDecoratorsAndFirstClientIsUnmanaged() throws Exception {
		DummyExchangeClient dummyClient = new DummyExchangeClient();
		dummyClient.close();

		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(dummyClient)
				.decoratedWith(DummyDecoratingExchangeClient.class)
				.decoratedWith(DummyDecoratingExchangeClient.class);

		ScopedResource<ExchangeClient> scopedClient = null;
		ExchangeClient client = null;
		ExchangeClient innerClient = null;
		ExchangeClient innermostClient = null;
		try {
			scopedClient = builder.build();
			client = scopedClient.unwrap();
			innerClient = ((DummyDecoratingExchangeClient) client).getExchangeClient();
			innermostClient = ((DummyDecoratingExchangeClient) innerClient).getExchangeClient();
		} finally {
			if (null != scopedClient) {
				scopedClient.closeIfManaged();
			}
		}

		assertThat(client.getClass(), equalTo(DummyDecoratingExchangeClient.class));
		assertThat(innerClient.getClass(), equalTo(DummyDecoratingExchangeClient.class));
		assertThat(innermostClient.getClass(), equalTo(DummyExchangeClient.class));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDecorateWithMultipleDecoratorsAndTypesAndFirstClientIsUnmanaged() throws Exception {
		DummyExchangeClient dummyClient = new DummyExchangeClient();
		dummyClient.close();

		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(dummyClient)
				.decoratedWith(OtherDecoratingExchangeClient.class)
				.decoratedWith(DummyDecoratingExchangeClient.class);

		ScopedResource<ExchangeClient> scopedClient = null;
		ExchangeClient client = null;
		ExchangeClient innerClient = null;
		ExchangeClient innermostClient = null;
		try {
			scopedClient = builder.build();
			client = scopedClient.unwrap();
			innerClient = ((DummyDecoratingExchangeClient) client).getExchangeClient();
			innermostClient = ((OtherDecoratingExchangeClient) innerClient).getExchangeClient();
		} finally {
			if (null != scopedClient) {
				scopedClient.closeIfManaged();
			}
		}

		assertThat(client.getClass(), equalTo(DummyDecoratingExchangeClient.class));
		assertThat(innerClient.getClass(), equalTo(OtherDecoratingExchangeClient.class));
		assertThat(innermostClient.getClass(), equalTo(DummyExchangeClient.class));
	}
}
