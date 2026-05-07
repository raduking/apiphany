package org.apiphany.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClientBuilderTest.Builders.ThrowingExchangeClientBuilder;
import org.apiphany.client.ExchangeClientBuilderTest.Clients.DummyDecoratingExchangeClient;
import org.apiphany.client.ExchangeClientBuilderTest.Clients.DummyExchangeClient;
import org.apiphany.client.ExchangeClientBuilderTest.Clients.InvalidDecoratingExchangeClient;
import org.apiphany.client.ExchangeClientBuilderTest.Clients.MultipleArgumentConstructorExchangeClient;
import org.apiphany.client.ExchangeClientBuilderTest.Clients.NotClosingDecoratingExchangeClient;
import org.apiphany.client.ExchangeClientBuilderTest.Clients.OtherDecoratingExchangeClient;
import org.junit.jupiter.api.Nested;
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

	private static final String BOOM_TEST_EXCEPTION = "BOOM! Simulated exception";
	private static final List<String> EXCLUSIVE_FIELDS = List.of("exchangeClientClass", "exchangeClient", "exchangeClientResource");
	private static final String EXPECTED_EXCLUSIVE_FIELDS_EXCEPTION_MESSAGE =
			Messages.message("One and only one of the following fields must be set: {}", EXCLUSIVE_FIELDS);

	private static final String TEST_STRING = "test";
	private static final int TEST_INT_42 = 42;

	interface Clients {

		static class DummyExchangeClient implements ExchangeClient {

			private boolean closed = false;

			@Override
			public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
				return null;
			}

			@Override
			public void close() {
				this.closed = true;
			}

			public boolean isClosed() {
				return closed;
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

		static class MultipleArgumentConstructorExchangeClient implements ExchangeClient {

			private final ClientProperties properties;
			private final Integer arg1;
			private final String arg2;

			private boolean closed = false;

			public MultipleArgumentConstructorExchangeClient(final ClientProperties properties, final Integer arg1, final String arg2) {
				this.properties = properties;
				this.arg1 = arg1;
				this.arg2 = arg2;
			}

			@Override
			public <T, U> ApiResponse<U> exchange(final ApiRequest<T> request) {
				return null;
			}

			@Override
			public void close() throws Exception {
				this.closed = true;
			}

			public boolean isClosed() {
				return closed;
			}

			public ClientProperties getProperties() {
				return properties;
			}

			public Integer getArg1() {
				return arg1;
			}

			public String getArg2() {
				return arg2;
			}
		}
	}

	interface Builders {

		static class ThrowingExchangeClientBuilder extends ExchangeClientBuilder {

			public ThrowingExchangeClientBuilder() {
				super();
			}

			@Override
			public ScopedResource<ExchangeClient> build() {
				throw new RuntimeException(BOOM_TEST_EXCEPTION);
			}
		}
	}

	@Nested
	class ExclusiveFieldsValidationTests {

		@Test
		void shouldNotAllowBothExchangeClientAndExchangeClientClassToBeNonNull() throws Exception {
			try (ExchangeClient exchangeClient = new DummyExchangeClient()) {
				ExchangeClientBuilder builder = ExchangeClientBuilder.create()
						.client(ExchangeClient.class)
						.client(exchangeClient);

				IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

				assertThat(exception.getMessage(), equalTo(Messages.message(EXPECTED_EXCLUSIVE_FIELDS_EXCEPTION_MESSAGE)));
			}
		}

		@Test
		void shouldNotAllowAllRequiredFieldsToBeNull() throws Exception {
			try (ExchangeClient exchangeClient = new DummyExchangeClient()) {
				ExchangeClientBuilder builder = ExchangeClientBuilder.create();

				IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

				assertThat(exception.getMessage(), equalTo(EXPECTED_EXCLUSIVE_FIELDS_EXCEPTION_MESSAGE));
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
					assertThat(e.getMessage(), equalTo(Messages.message(EXPECTED_EXCLUSIVE_FIELDS_EXCEPTION_MESSAGE)));
					errorHandlerCallCount.incrementAndGet();
				};
				ScopedResource<ExchangeClient> result = builder.build(errorHandler);

				assertThat(result, equalTo(null));
				assertThat(errorHandlerCallCount.get(), equalTo(1));
			}
		}
	}

	@Nested
	class ClientConstructionTests {

		@Test
		@SuppressWarnings("resource")
		void shouldConstructClientUsingClientClass() throws Exception {
			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(DummyExchangeClient.class);

			ExchangeClient client = null;
			try (ScopedResource<ExchangeClient> scopedClient = builder.build()) {
				client = scopedClient.unwrap();
			}

			assertThat(client.getClass(), equalTo(DummyExchangeClient.class));
			DummyExchangeClient dummyClient = (DummyExchangeClient) client;

			assertThat(dummyClient.isClosed(), equalTo(true));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldConstructClientUsingClientInstance() throws Exception {
			DummyExchangeClient dummyClient = new DummyExchangeClient();

			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(dummyClient);

			boolean closed = false;
			ExchangeClient client = null;
			try (ScopedResource<ExchangeClient> scopedClient = builder.build()) {
				client = scopedClient.unwrap();
			} finally {
				closed = dummyClient.isClosed();
				dummyClient.close();
			}

			assertThat(client.getClass(), equalTo(DummyExchangeClient.class));
			assertThat(closed, equalTo(false));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldConstructClientUsingMnagedScopedResource() throws Exception {
			DummyExchangeClient dummyClient = new DummyExchangeClient();
			ScopedResource<ExchangeClient> clientResource = ScopedResource.managed(dummyClient);

			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(clientResource);

			ExchangeClient client = null;
			try (ScopedResource<ExchangeClient> scopedClient = builder.build()) {
				client = scopedClient.unwrap();
			}

			assertThat(client.getClass(), equalTo(DummyExchangeClient.class));
			assertThat(dummyClient.isClosed(), equalTo(true));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldConstructClientUsingUnmanagedScopedResource() throws Exception {
			DummyExchangeClient dummyClient = new DummyExchangeClient();
			ScopedResource<ExchangeClient> clientResource = ScopedResource.unmanaged(dummyClient);

			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(clientResource);

			boolean closed = false;
			ExchangeClient client = null;
			try (ScopedResource<ExchangeClient> scopedClient = builder.build()) {
				client = scopedClient.unwrap();
			} finally {
				closed = dummyClient.isClosed();
				dummyClient.close();
			}

			assertThat(client.getClass(), equalTo(DummyExchangeClient.class));
			assertThat(closed, equalTo(false));
		}

		@Test
		@SuppressWarnings("resource")
		void shouldConstructClientUsingClientClassWithMultipleArgumentConstructor() throws Exception {
			Integer arg1 = TEST_INT_42;
			String arg2 = TEST_STRING;
			ClientProperties clientProperties = new ClientProperties();

			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(MultipleArgumentConstructorExchangeClient.class)
					.arguments(arg1, arg2)
					.properties(clientProperties);

			ExchangeClient client = null;
			try (ScopedResource<ExchangeClient> scopedClient = builder.build()) {
				client = scopedClient.unwrap();
			}

			assertThat(client.getClass(), equalTo(MultipleArgumentConstructorExchangeClient.class));
			MultipleArgumentConstructorExchangeClient multiArgClient = (MultipleArgumentConstructorExchangeClient) client;

			assertThat(multiArgClient.getProperties(), equalTo(clientProperties));
			assertThat(multiArgClient.getArg1(), equalTo(TEST_INT_42));
			assertThat(multiArgClient.getArg2(), equalTo(TEST_STRING));
		}

		@Test
		void shouldThrowExceptionConstructClientUsingClientClassWithMultipleArgumentConstructorAndWrongArgumentType() {
			Integer arg1 = TEST_INT_42;
			String arg2 = TEST_STRING;
			ClientProperties clientProperties = new ClientProperties();

			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.client(MultipleArgumentConstructorExchangeClient.class)
					.arguments(arg2, arg1)
					.properties(clientProperties);

			IllegalStateException exception = assertThrows(IllegalStateException.class, builder::build);

			assertThat(exception.getMessage(), equalTo(Messages.message(
					"Client {} must have a constructor matching the client arguments provided in the builder: {}",
					MultipleArgumentConstructorExchangeClient.class.getName(), List.of(ClientProperties.class, String.class, Integer.class))));
		}
	}

	@Nested
	class DecoratingExchangeClientTests {

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
	}

	@Nested
	class ManagedClientConstructionValidationTests {

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
	}

	@Nested
	class DelegateHandlingTests {

		@Test
		@SuppressWarnings("resource")
		void shouldReturnNullAndCallErrorHandlerWhenDelegateBuildingThrowsException() {
			ExchangeClientBuilder builder = ExchangeClientBuilder.create()
					.delegate(new ThrowingExchangeClientBuilder());

			AtomicInteger errorHandlerCallCount = new AtomicInteger(0);
			Consumer<Exception> errorHandler = e -> {
				assertThat(e.getMessage(), equalTo(BOOM_TEST_EXCEPTION));
				errorHandlerCallCount.incrementAndGet();
			};

			ScopedResource<ExchangeClient> scopedClient = builder.build(errorHandler);

			assertThat(scopedClient, equalTo(null));
			assertThat(errorHandlerCallCount.get(), equalTo(1));
		}
	}
}
