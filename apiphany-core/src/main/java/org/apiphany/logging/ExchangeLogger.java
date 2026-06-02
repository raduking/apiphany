package org.apiphany.logging;

import java.time.Duration;

import org.apiphany.ApiMessage;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;
import org.apiphany.lang.Strings;
import org.morphix.lang.Nullables;
import org.morphix.lang.Temporals;
import org.morphix.lang.function.LoggingFunction;
import org.morphix.reflection.Constructors;

/**
 * A utility class for logging API requests and responses, including success and error cases. This class provides
 * methods to log request details, response details, and exceptions in a structured format.
 * <p>
 * Request/response body logging is configurable through {@link ExchangeLoggingProperties}.
 * <p>
 * TODO: log headers on multiple lines for better readability.
 * <p>
 * TODO: implement injectable exchange logger.
 *
 * @author Radu Sebastian LAZIN
 */
public class ExchangeLogger {

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
	public static final String LOG_SEPARATOR = String.valueOf(LOG_SEPARATOR_CHAR).repeat(LOG_SEPARATOR_LENGTH);

	/**
	 * The log message format for successful requests.
	 */
	private static final String LOG_MESSAGE_SUCCESS = Strings.EOL
			+ LOG_SEPARATOR + Strings.EOL
			+ "CLIENT: {}" + Strings.EOL
			+ "[REQUEST]" + Strings.EOL
			+ "METHOD: {}" + Strings.EOL
			+ "URL: {}" + Strings.EOL
			+ "PARAMETERS: {}" + Strings.EOL
			+ "HEADERS: {}" + Strings.EOL
			+ "BODY: {}" + Strings.EOL
			+ "[RESPONSE]" + Strings.EOL
			+ "STATUS: {}" + Strings.EOL
			+ "HEADERS: {}" + Strings.EOL
			+ "BODY: {}" + Strings.EOL
			+ "DURATION: {}s" + Strings.EOL
			+ LOG_SEPARATOR;

	/**
	 * The log message format for failed requests.
	 */
	private static final String LOG_MESSAGE_ERROR = Strings.EOL
			+ LOG_SEPARATOR + Strings.EOL
			+ "CLIENT: {}" + Strings.EOL
			+ "[REQUEST]" + Strings.EOL
			+ "METHOD: {}" + Strings.EOL
			+ "URL: {}" + Strings.EOL
			+ "PARAMETERS: {}" + Strings.EOL
			+ "HEADERS: {}" + Strings.EOL
			+ "REQUEST BODY: {}" + Strings.EOL
			+ "[RESPONSE]" + Strings.EOL
			+ "STATUS: {}" + Strings.EOL
			+ "EXCEPTION: {}" + Strings.EOL
			+ "DURATION: {}s" + Strings.EOL
			+ LOG_SEPARATOR;

	/**
	 * Logs all information for a successful HTTP request.
	 *
	 * @param <T> the type of the request/response body.
	 *
	 * @param loggingFunction the logging function used to output the log message
	 * @param apiClientClass the API class of the client making the request
	 * @param exchangeClient the exchange client that made the request
	 * @param apiRequest the API request object
	 * @param apiResponse the API response object
	 * @param duration the duration of the request
	 */
	public static <T> void logSuccess(
			final LoggingFunction loggingFunction,
			final Class<?> apiClientClass,
			final ExchangeClient exchangeClient,
			final ApiRequest<T> apiRequest,
			final ApiResponse<T> apiResponse,
			final Duration duration) {
		loggingFunction.log(LOG_MESSAGE_SUCCESS,
				apiClientClass,
				apiRequest.getMethod(),
				apiRequest.getUrl(),
				apiRequest.getDisplayParams(),
				apiRequest.getDisplayHeaders(),
				describeBody(apiRequest, exchangeClient),
				Nullables.apply(apiResponse, ApiResponse::getStatus),
				Nullables.apply(apiResponse, ApiResponse::getDisplayHeaders),
				describeBody(apiResponse, exchangeClient),
				Temporals.toSeconds(duration.toMillis()));
	}

	/**
	 * Logs all information for a failed HTTP request.
	 *
	 * @param <T> the type of the request.
	 *
	 * @param loggingFunction the logging function used to output the log message
	 * @param apiClientClass the API class of the client making the request
	 * @param exchangeClient the exchange client used for this request
	 * @param apiRequest the API request object
	 * @param apiResponse the API response object, if available
	 * @param duration the duration of the request
	 */
	public static <T> void logError(
			final LoggingFunction loggingFunction,
			final Class<?> apiClientClass,
			final ExchangeClient exchangeClient,
			final ApiRequest<T> apiRequest,
			final ApiResponse<T> apiResponse,
			final Duration duration) {
		Exception exception = Nullables.apply(apiResponse, ApiResponse::getException);
		loggingFunction.log(LOG_MESSAGE_ERROR,
				apiClientClass,
				apiRequest.getMethod(),
				apiRequest.getUrl(),
				apiRequest.getDisplayParams(),
				apiRequest.getDisplayHeaders(),
				describeBody(apiRequest, exchangeClient),
				Nullables.apply(apiResponse, ApiResponse::getStatus),
				exception,
				Temporals.toSeconds(duration.toMillis()));
		loggingFunction.log("{}", Nullables.apply(apiResponse, ApiResponse::getErrorMessage), exception);
	}

	/**
	 * Describes the body of a request or response based on the logging configuration of the exchange client.
	 *
	 * @param exchangeClient the exchange client used for this request
	 * @param body the body to describe.
	 * @return a string description of the body, or "<omitted>" if body logging is disabled.
	 */
	private static <T> String describeBody(final ApiMessage<T> apiMessage, final ExchangeClient exchangeClient) {
		if (null == apiMessage) {
			return null;
		}
		T body = apiMessage.getBody();
		if (null == body) {
			return null;
		}
		ExchangeLoggingProperties loggingProperties = exchangeClient.getCustomProperties(ExchangeLoggingProperties.class);
		Logging.Mode bodyLoggingMode = Nullables.apply(loggingProperties, ExchangeLoggingProperties::getBodyLoggingMode);
		if (null == bodyLoggingMode || bodyLoggingMode == Logging.Mode.FULL) {
			return body.toString();
		}
		if (bodyLoggingMode == Logging.Mode.NONE) {
			return "<omitted>";
		}
		return Logging.describeInput(body, LoggingFormat.DEFAULT,
				Logging.Include.LENGTH,
				Logging.Include.HASH);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ExchangeLogger() {
		throw Constructors.unsupportedOperationException();
	}
}
