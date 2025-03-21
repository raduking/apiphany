package org.apiphany;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apiphany.http.HttpHeader;
import org.apiphany.lang.Strings;
import org.apiphany.lang.Temporals;
import org.apiphany.lang.collections.Maps;
import org.morphix.lang.Nullables;
import org.morphix.reflection.Constructors;

/**
 * A utility class for logging HTTP requests and responses, including success and error cases. This class provides
 * methods to log request details, response details, and exceptions in a structured format.
 *
 * @author Radu Sebastian LAZIN
 */
public class RequestLogger {

	/**
	 * The string used to redact sensitive information in logs.
	 */
	public static final String REDACTED = "REDACTED";

	/**
	 * The length of the log separator line.
	 */
	public static final int LOG_SEPARATOR_LENGTH = 128;

	/**
	 * The character used to create the log separator line.
	 */
	public static final char LOG_SEPARATOR_CHAR = '-';

	/**
	 * The log separator line, created by repeating {@link #LOG_SEPARATOR_CHAR} for {@link #LOG_SEPARATOR_LENGTH} times.
	 */
	public static final String LOG_SEPARATOR = StringUtils.repeat(LOG_SEPARATOR_CHAR, LOG_SEPARATOR_LENGTH);

	/**
	 * The log message format for successful requests.
	 */
	private static final String LOG_MESSAGE_SUCCESS = Strings.EOL
			+ LOG_SEPARATOR + Strings.EOL
			+ "CLIENT: {}" + Strings.EOL
			+ "METHOD: {}" + Strings.EOL
			+ "URL: {}" + Strings.EOL
			+ "HEADERS: {}" + Strings.EOL
			+ "PARAMETERS: {}" + Strings.EOL
			+ "REQUEST BODY: {}" + Strings.EOL
			+ "RESPONSE HEADERS: {}" + Strings.EOL
			+ "RESPONSE BODY: {}" + Strings.EOL
			+ "DURATION: {}s" + Strings.EOL
			+ LOG_SEPARATOR;

	/**
	 * The log message format for failed requests.
	 */
	private static final String LOG_MESSAGE_ERROR = Strings.EOL
			+ LOG_SEPARATOR + Strings.EOL
			+ "CLIENT: {}" + Strings.EOL
			+ "METHOD: {}" + Strings.EOL
			+ "URL: {}" + Strings.EOL
			+ "HEADERS: {}" + Strings.EOL
			+ "PARAMETERS: {}" + Strings.EOL
			+ "REQUEST BODY: {}" + Strings.EOL
			+ "EXCEPTION: {}" + Strings.EOL
			+ "DURATION: {}s" + Strings.EOL
			+ LOG_SEPARATOR;

	/**
	 * Logs all information for a successful HTTP request.
	 *
	 * @param <T> the type of the request/response body.
	 *
	 * @param loggingFunction the logging function used to output the log message.
	 * @param apiClient the API client making the request.
	 * @param apiRequest the request properties.
	 * @param apiResponse the response object.
	 * @param duration the duration of the request.
	 */
	public static <T> void logSuccess(
			final LoggingFunction loggingFunction,
			final ApiClient apiClient,
			final ApiRequest<T> apiRequest,
			final ApiResponse<T> apiResponse,
			final Duration duration) {
		loggingFunction.level(LOG_MESSAGE_SUCCESS,
				apiClient.getClass(),
				apiRequest.getHttpMethod(),
				apiRequest.getUrl(),
				toRedactedString(apiRequest.getHeaders()),
				apiRequest.getParams(),
				apiRequest.getBody(),
				Nullables.apply(apiResponse, response -> toRedactedString(response.getHeaders())),
				Nullables.apply(apiResponse, ApiResponse::getBody),
				Temporals.toSeconds(duration.toMillis()));
	}

	/**
	 * Logs all information for a failed HTTP request.
	 *
	 * @param <T> the type of the request.
	 *
	 * @param loggingFunction the logging function used to output the log message.
	 * @param apiClient the API client making the request.
	 * @param apiRequest the request properties.
	 * @param duration the duration of the request.
	 * @param exception the exception that caused the request to fail.
	 */
	public static <T> void logError(
			final LoggingFunction loggingFunction,
			final ApiClient apiClient,
			final ApiRequest<T> apiRequest,
			final Duration duration,
			final Exception exception) {
		loggingFunction.level(LOG_MESSAGE_ERROR,
				apiClient.getClass(),
				apiRequest.getHttpMethod(),
				apiRequest.getUrl(),
				toRedactedString(apiRequest.getHeaders()),
				apiRequest.getParams(),
				apiRequest.getBody(),
				exception,
				Temporals.toSeconds(duration.toMillis()));
	}

	/**
	 * Transforms the HTTP headers into a string representation, redacting sensitive information such as Authorization
	 * headers.
	 *
	 * @param headers the headers as a multi-value map.
	 * @return the headers as a string, with sensitive information redacted.
	 */
	public static String toRedactedString(final Map<String, List<String>> headers) {
		return Maps.safe(headers).entrySet().stream().map(entry -> {
			StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey()).append(":");
			List<String> value = entry.getValue();
			if (HttpHeader.AUTHORIZATION.matches(entry.getKey())) {
				value = Collections.singletonList(REDACTED);
			}
			sb.append("\"");
			sb.append(StringUtils.join(value, ", "));
			sb.append("\"");
			return sb.toString();
		}).toList().toString();
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private RequestLogger() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Functional interface for logging messages with a specific format and arguments.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	public interface LoggingFunction {

		/**
		 * Logs a message with the specified format and arguments.
		 *
		 * @param format the log message format.
		 * @param arguments the arguments to include in the log message.
		 */
		void level(String format, Object... arguments);
	}
}