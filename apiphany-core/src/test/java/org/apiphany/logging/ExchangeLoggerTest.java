package org.apiphany.logging;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.http.HttpMethod;
import org.apiphany.http.HttpStatus;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.Test;
import org.morphix.lang.Temporals;
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
		assertThat(call.arguments.length, equalTo(0));
		assertThat(call.format, containsString("BODY: request-body"));
		assertThat(call.format, containsString("BODY: response-body"));
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
		assertThat(call.arguments.length, equalTo(0));
		assertThat(call.format, containsString("BODY: " + Logging.describeInput("request-body", LoggingFormat.DEFAULT,
				Logging.Include.LENGTH,
				Logging.Include.HASH)));
		assertThat(call.format, containsString("BODY: " + Logging.describeInput("response-body", LoggingFormat.DEFAULT,
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
		assertThat(call.arguments.length, equalTo(0));
		assertThat(call.format, containsString("BODY: <omitted>"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldLogCompleteMessageFormatForSuccessPath() {
		RecordingLoggingFunction loggingFunction = new RecordingLoggingFunction();
		ExchangeClient exchangeClient = new DummyExchangeClient(Logging.Mode.FULL);
		Map<String, List<String>> params = new LinkedHashMap<>();
		params.put("id", List.of("123"));
		params.put("type", List.of("full"));
		Map<String, List<String>> headers = new LinkedHashMap<>();
		headers.put("Accept", List.of("application/json"));
		headers.put("X-Correlation-Id", List.of("abc"));
		ApiRequest<String> request = request("request-body", params, headers);
		ApiResponse<String> response = ApiResponse.create("response-body")
				.status(HttpStatus.OK)
				.headers(headers)
				.build();
		Duration duration = Duration.ofMillis(1234);

		ExchangeLogger.logSuccess(loggingFunction, getClass(), exchangeClient, request, response, duration);

		LogCall call = loggingFunction.calls.getFirst();
		assertThat(call.arguments.length, equalTo(0));
		assertThat(call.format, equalTo(expectedSuccessMessage(getClass(), request, response, duration, "request-body", "response-body")));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldLogNullResponseBodyWhenApiResponseIsNull() {
		RecordingLoggingFunction loggingFunction = new RecordingLoggingFunction();
		ExchangeClient exchangeClient = new DummyExchangeClient(Logging.Mode.FULL);
		ApiRequest<String> request = request("request-body");
		Duration duration = Duration.ofSeconds(1);

		ExchangeLogger.logSuccess(loggingFunction, getClass(), exchangeClient, request, null, duration);

		LogCall call = loggingFunction.calls.getFirst();
		assertThat(call.arguments.length, equalTo(0));
		assertThat(call.format, containsString("STATUS: null"));
		assertThat(call.format, containsString("HEADERS: null"));
		assertThat(call.format, containsString("BODY: null"));
	}

	@Test
	@SuppressWarnings("resource")
	void shouldLogCompleteMessageFormatForErrorPath() {
		RecordingLoggingFunction loggingFunction = new RecordingLoggingFunction();
		ExchangeClient exchangeClient = new DummyExchangeClient(Logging.Mode.FULL);
		Map<String, List<String>> params = new LinkedHashMap<>();
		params.put("id", List.of("123"));
		Map<String, List<String>> headers = new LinkedHashMap<>();
		headers.put("Accept", List.of("application/json"));
		ApiRequest<String> request = request("request-body", params, headers);
		RuntimeException exception = new RuntimeException("Boom");
		ApiResponse<String> response = ApiResponse.<String>builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.exception(exception)
				.errorMessage("Error happened")
				.build();
		Duration duration = Duration.ofMillis(500);

		ExchangeLogger.logError(loggingFunction, getClass(), exchangeClient, request, response, duration);

		LogCall structuredLog = loggingFunction.calls.getFirst();
		assertThat(structuredLog.arguments.length, equalTo(0));
		assertThat(structuredLog.format,
				equalTo(expectedErrorMessage(getClass(), request, response, duration, "request-body", exception)));

		LogCall errorLine = loggingFunction.calls.get(1);
		assertThat(errorLine.format, equalTo("{}"));
		assertThat(errorLine.arguments.length, equalTo(2));
		assertThat(errorLine.arguments[0], equalTo("Error happened"));
		assertThat(errorLine.arguments[1], equalTo(exception));
	}

	private static ApiRequest<String> request(final String body) {
		return request(body, new LinkedHashMap<>(), new LinkedHashMap<>());
	}

	private static ApiRequest<String> request(final String body,
			final Map<String, List<String>> displayParams,
			final Map<String, List<String>> displayHeaders) {
		return new RequestForTest(HttpMethod.POST, "https://example.org", body, displayParams, displayHeaders);
	}

	private static ApiResponse<String> response(final String body) {
		return ApiResponse.create(body)
				.status(HttpStatus.OK)
				.build();
	}

	private record LogCall(String format, Object[] arguments) {
		// empty
	}

	private static String expectedSuccessMessage(
			final Class<?> apiClientClass,
			final ApiRequest<String> request,
			final ApiResponse<String> response,
			final Duration duration,
			final String requestBody,
			final String responseBody) {
		return Strings.EOL
				+ ExchangeLogger.LOG_SEPARATOR + Strings.EOL
				+ "CLIENT: " + apiClientClass + Strings.EOL
				+ "[REQUEST]" + Strings.EOL
				+ "METHOD: " + request.getMethod() + Strings.EOL
				+ "URL: " + request.getUrl() + Strings.EOL
				+ "PARAMETERS: " + request.getDisplayParams() + Strings.EOL
				+ "HEADERS: " + request.getDisplayHeaders() + Strings.EOL
				+ "BODY: " + requestBody + Strings.EOL
				+ "[RESPONSE]" + Strings.EOL
				+ "STATUS: " + response.getStatus() + Strings.EOL
				+ "HEADERS: " + response.getDisplayHeaders() + Strings.EOL
				+ "BODY: " + responseBody + Strings.EOL
				+ "DURATION: " + Temporals.toSeconds(duration.toMillis()) + "s" + Strings.EOL
				+ ExchangeLogger.LOG_SEPARATOR;
	}

	private static String expectedErrorMessage(
			final Class<?> apiClientClass,
			final ApiRequest<String> request,
			final ApiResponse<String> response,
			final Duration duration,
			final String requestBody,
			final Exception exception) {
		return Strings.EOL
				+ ExchangeLogger.LOG_SEPARATOR + Strings.EOL
				+ "CLIENT: " + apiClientClass + Strings.EOL
				+ "[REQUEST]" + Strings.EOL
				+ "METHOD: " + request.getMethod() + Strings.EOL
				+ "URL: " + request.getUrl() + Strings.EOL
				+ "PARAMETERS: " + request.getDisplayParams() + Strings.EOL
				+ "HEADERS: " + request.getDisplayHeaders() + Strings.EOL
				+ "REQUEST BODY: " + requestBody + Strings.EOL
				+ "[RESPONSE]" + Strings.EOL
				+ "STATUS: " + response.getStatus() + Strings.EOL
				+ "HEADERS: " + response.getDisplayHeaders() + Strings.EOL
				+ "EXCEPTION: " + exception + Strings.EOL
				+ "DURATION: " + Temporals.toSeconds(duration.toMillis()) + "s" + Strings.EOL
				+ ExchangeLogger.LOG_SEPARATOR;
	}

	private static class RequestForTest extends ApiRequest<String> {

		private final Map<String, List<String>> displayParams;
		private final Map<String, List<String>> displayHeaders;

		RequestForTest(final HttpMethod method,
				final String url,
				final String body,
				final Map<String, List<String>> displayParams,
				final Map<String, List<String>> displayHeaders) {
			this.method = method;
			this.url = url;
			this.body = body;
			this.params = displayParams;
			this.headers.putAll(displayHeaders);
			this.displayParams = displayParams;
			this.displayHeaders = displayHeaders;
		}

		@Override
		public Map<String, List<String>> getDisplayParams() {
			return displayParams;
		}

		@Override
		public Map<String, List<String>> getDisplayHeaders() {
			return displayHeaders;
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
			ClientProperties.Logging properties = new ClientProperties.Logging();
			properties.getBody().setMode(bodyLoggingMode);
			this.clientProperties = new ClientProperties();
			this.clientProperties.setLogging(properties);
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
