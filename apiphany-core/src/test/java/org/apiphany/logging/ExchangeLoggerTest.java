package org.apiphany.logging;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.LoggingFunction;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link ExchangeLogger}.
 *
 * @author Radu Sebastian LAZIN
 */
class ExchangeLoggerTest {

	@Test
	void shouldThrowExceptionOnCallingConstructor() {
		UnsupportedOperationException unsupportedOperationException = assertDefaultConstructorThrows(ExchangeLogger.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldLogFullBodyWhenBodyLoggingModeIsFull() {
		RecordingLoggingFunction loggingFunction = new RecordingLoggingFunction();
		ExchangeClient exchangeClient = new DummyExchangeClient(Logging.Mode.FULL);
		ApiRequest<String> request = request("request-body");
		ApiResponse<String> response = response("response-body");

		ExchangeLogger.logSuccess(loggingFunction, getClass(), exchangeClient, request, response, Duration.ofSeconds(1));

		LogCall call = loggingFunction.calls.getFirst();
		assertThat(call.arguments[5], equalTo("request-body"));
		assertThat(call.arguments[8], equalTo("response-body"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldLogMetadataWhenBodyLoggingModeIsMetadata() {
		RecordingLoggingFunction loggingFunction = new RecordingLoggingFunction();
		ExchangeClient exchangeClient = new DummyExchangeClient(Logging.Mode.METADATA);
		ApiRequest<String> request = request("request-body");
		ApiResponse<String> response = response("response-body");

		ExchangeLogger.logSuccess(loggingFunction, getClass(), exchangeClient, request, response, Duration.ofSeconds(1));

		LogCall call = loggingFunction.calls.getFirst();
		assertThat(call.arguments[5], equalTo(Logging.describeInput("request-body", LoggingFormat.DEFAULT,
				Logging.Include.LENGTH,
				Logging.Include.HASH)));
		assertThat(call.arguments[8], equalTo(Logging.describeInput("response-body", LoggingFormat.DEFAULT,
				Logging.Include.LENGTH,
				Logging.Include.HASH)));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldLogOmittedWhenBodyLoggingModeIsNone() {
		RecordingLoggingFunction loggingFunction = new RecordingLoggingFunction();
		ExchangeClient exchangeClient = new DummyExchangeClient(Logging.Mode.NONE);
		ApiRequest<String> request = request("request-body");
		ApiResponse<String> response = response("response-body");

		ExchangeLogger.logSuccess(loggingFunction, getClass(), exchangeClient, request, response, Duration.ofSeconds(1));

		LogCall call = loggingFunction.calls.getFirst();
		assertThat(call.arguments[5], equalTo("<omitted>"));
		assertThat(call.arguments[8], equalTo("<omitted>"));
	}

	private static ApiRequest<String> request(final String body) {
		return new RequestForTest(HttpMethod.POST, "https://example.org", body);
	}

	private static ApiResponse<String> response(final String body) {
		return ApiResponse.create(body)
				.status(HttpStatus.OK)
				.build();
	}

	private record LogCall(String format, Object[] arguments) {
		// empty
	}

	private static class RequestForTest extends ApiRequest<String> {

		RequestForTest(final HttpMethod method, final String url, final String body) {
			this.method = method;
			this.url = url;
			this.body = body;
		}
	}

	private static class RecordingLoggingFunction implements LoggingFunction {

		private final List<LogCall> calls = new ArrayList<>();

		@Override
		public void log(final String format, final Object... arguments) {
			calls.add(new LogCall(format, arguments));
		}
	}

	private static class DummyExchangeClient implements ExchangeClient {

		private final ClientProperties clientProperties;

		DummyExchangeClient(final Logging.Mode bodyLoggingMode) {
			ExchangeLoggingProperties properties = new ExchangeLoggingProperties();
			properties.setBodyLoggingMode(bodyLoggingMode);
			this.clientProperties = new ClientProperties();
			this.clientProperties.setCustomProperties(properties);
		}

		@Override
		public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ClientProperties getClientProperties() {
			return clientProperties;
		}

		@Override
		public void close() {
			// empty
		}
	}
}
