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
 * Utility class for logging requests.
 *
 * @author Radu Sebastian LAZIN
 */
public class RequestLogger {

	public static final String REDACTED = "REDACTED";

	public static final int LOG_SEPARATOR_LENGTH = 128;
	public static final char LOG_SEPARATOR_CHAR = '-';

	public static final String LOG_SEPARATOR = StringUtils.repeat(LOG_SEPARATOR_CHAR, LOG_SEPARATOR_LENGTH);

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
	 * Logs all information for a request.
	 *
	 * @param <T> response type
	 *
	 * @param loggingFunction logging function used for logging
	 * @param apiClient the API client
	 * @param apiRequest request properties
	 * @param apiResponse response object
	 * @param duration duration of the request
	 */
	public static <T> void logSuccess(
			final LoggingFunction loggingFunction,
			final ApiClient apiClient,
			final ApiRequest apiRequest,
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
	 * Logs all information for a request.
	 *
	 * @param loggingFunction logging function used for logging
	 * @param apiClient the API client
	 * @param apiRequest request properties
	 * @param duration duration of the request
	 * @param exception exception
	 */
	public static void logError(
			final LoggingFunction loggingFunction,
			final ApiClient apiClient,
			final ApiRequest apiRequest,
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
	 * Transforms the HTTP headers to {@link String} and redacts any sensible information like Authorization and such.
	 *
	 * @param headers headers as a multi value map
	 * @return the headers as string
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
	 * Private constructor.
	 */
	private RequestLogger() {
		throw Constructors.unsupportedOperationException();
	}

	/**
	 * Functional interface to transmit the logging function from the caller.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	@FunctionalInterface
	public interface LoggingFunction {

		void level(String format, Object... arguments);

	}

}
