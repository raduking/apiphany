package org.apiphany.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.junit.jupiter.api.Test;
import org.morphix.lang.Messages;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.leak.ResourceLeakTracker;
import org.morphix.lang.resource.ScopedResource;
import org.morphix.reflection.Fields;

/**
 * Test class for {@link ExchangeClientBuilder}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExchangeClientBuilderTest {

	private static final String BOOM_TEST_EXCEPTION = "BOOM! Test exception";

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

	static class InvalidDecoratingExchangeClient extends DecoratingExchangeClient {

		private String invalidParameter;

		public InvalidDecoratingExchangeClient(final String invalidParameter) {
			super((ExchangeClient) null);
			this.invalidParameter = invalidParameter;
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
			return null;
		}

		public String getInvalidParameter() {
			return invalidParameter;
		}
	}

	static class NotClosingDecoratingExchangeClient extends DecoratingExchangeClient {

		public NotClosingDecoratingExchangeClient(final ScopedResource<ExchangeClient> delegate) {
			super(delegate);
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
			return null;
		}

		@Override
		public void close() throws Exception {
			super.close();
			throw new RuntimeException(BOOM_TEST_EXCEPTION);
		}
	}

	static class ThrowingExchangeClientBuilder extends ExchangeClientBuilder {

		public ThrowingExchangeClientBuilder() {
			super();
		}

		@Override
		public ScopedResource<ExchangeClient> build() {
			throw new RuntimeException(BOOM_TEST_EXCEPTION);
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
	@SuppressWarnings("resource")
	void shouldNotAllowBothExchangeClientAndExchangeClientClassToBeNonNullAndCallExceptionHandler() throws Exception {
		try (ExchangeClient exchangeClient = new DummyExchangeClient()) {
			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(ExchangeClient.class)
					.client(exchangeClient);

			AtomicInteger errorHandlerCallCount = new AtomicInteger(0);
			Consumer<Exception> errorHandler = e -> {
				assertThat(e.getMessage(), equalTo("Cannot set both exchange client instance and exchange client class"));
				errorHandlerCallCount.incrementAndGet();
			};
			ScopedResource<ExchangeClient> result = builder.build(errorHandler);

			assertThat(result, equalTo(null));
			assertThat(errorHandlerCallCount.get(), equalTo(1));
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
	void shouldCloseResourceWhenDecoratingWithInvalidDecoratorsAndFirstClientIsManaged() {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.decoratedWith(DummyDecoratingExchangeClient.class)
				.decoratedWith(InvalidDecoratingExchangeClient.class);

		ScopedResource<ExchangeClient> scopedClient = builder.build(Consumers.noConsumer());
		ExchangeClient client = scopedClient.unwrap();
		ExchangeClient innerClient = ((DummyDecoratingExchangeClient) client).getExchangeClient();
		ResourceLeakTracker tracker = Fields.IgnoreAccess.get(scopedClient, "leakTracker");

		assertThat(client.getClass(), equalTo(DummyDecoratingExchangeClient.class));
		assertThat(innerClient.getClass(), equalTo(DummyExchangeClient.class));
		assertThat(tracker.isClosed(), equalTo(true));
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

	@Test
	void shouldThrowExceptionWhenDecoratingWithInvalidDecoratorAndFirstClientIsManaged() {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.decoratedWith(InvalidDecoratingExchangeClient.class);

		IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

		assertThat(exception.getMessage(), equalTo("Decorating exchange client class "
				+ InvalidDecoratingExchangeClient.class.getName() + " must have a constructor with one parameter of type "
				+ ScopedResource.class.getName() + "<" + ExchangeClient.class.getName() + ">"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldHandleExceptionAndCloseResourceWhenDecoratingWithInvalidDecoratorAndFirstClientIsManaged() {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.decoratedWith(NotClosingDecoratingExchangeClient.class)
				.decoratedWith(InvalidDecoratingExchangeClient.class);

		AtomicInteger errorHandlerCallCount = new AtomicInteger(0);
		Consumer<Exception> errorHandler = e -> {
			assertThat(e.getMessage(), equalTo("Decorating exchange client class "
					+ InvalidDecoratingExchangeClient.class.getName() + " must have a constructor with one parameter of type "
					+ ScopedResource.class.getName() + "<" + ExchangeClient.class.getName() + ">"));
			errorHandlerCallCount.incrementAndGet();
		};
		ScopedResource<ExchangeClient> scopedClient = builder.build(errorHandler);
		ResourceLeakTracker tracker = Fields.IgnoreAccess.get(scopedClient, "leakTracker");

		assertThat(errorHandlerCallCount.get(), equalTo(1));
		assertThat(tracker.isClosed(), equalTo(true));
	}

	@Test
	void shouldThrowExceptionWhenDecoratingWithInvalidDecoratorAndFirstClientIsUnmanaged() {
		DummyExchangeClient dummyClient = new DummyExchangeClient();
		dummyClient.close();

		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(dummyClient)
				.decoratedWith(InvalidDecoratingExchangeClient.class);

		IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

		assertThat(exception.getMessage(),
				equalTo(Messages.message("Decorating exchange client class {} must have a constructor with one parameter of type {} or {}<{}>",
						InvalidDecoratingExchangeClient.class.getName(), ExchangeClient.class.getName(), ScopedResource.class.getName(),
						ExchangeClient.class.getName())));
	}

	@Test
	void shouldThrowExceptionWhenBuildingManagedClientWithoutClientPropertiesSetAndNoDefaultConstructor() {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(InvalidDecoratingExchangeClient.class);

		IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

		assertThat(exception.getMessage(),
				equalTo(Messages.message(
						"When client properties are not set exchange client class {} must have a default constructor",
						InvalidDecoratingExchangeClient.class.getName())));
	}

	@Test
	void shouldThrowExceptionWhenBuildingManagedClientWithoutClientPropertiesSetAndConstructorWithClientProperties() {
		ClientProperties clientProperties = new ClientProperties();
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.client(DummyExchangeClient.class)
				.properties(clientProperties);

		IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

		assertThat(exception.getMessage(),
				equalTo(Messages.message(
						"When client properties are set exchange client class {} must not have a constructor with one parameter of type {}",
						DummyExchangeClient.class.getName(), ClientProperties.class.getName())));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNullAndCallErrorHandlerWhenDelegateBuildingThrowsException() {
		ExchangeClientBuilder builder = ExchangeClientBuilder.create()
				.delegate(new ThrowingExchangeClientBuilder());

		AtomicInteger errorHandlerCallCount = new AtomicInteger(0);
		Consumer<Exception> errorHandler = e -> {
			assertThat(e.getMessage(), equalTo("BOOM! Test exception"));
			errorHandlerCallCount.incrementAndGet();
		};

		ScopedResource<ExchangeClient> scopedClient = builder.build(errorHandler);

		assertThat(scopedClient, equalTo(null));
		assertThat(errorHandlerCallCount.get(), equalTo(1));
	}
}
