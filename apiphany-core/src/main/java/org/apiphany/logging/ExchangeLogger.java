package org.apiphany.logging;

import java.time.Duration;
import java.util.function.Predicate;

import org.apiphany.ApiMessage;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.lang.Strings;
import org.apiphany.security.Sensitive;
import org.morphix.lang.Messages;
import org.morphix.lang.Nullables;
import org.morphix.lang.Temporals;
import org.morphix.lang.function.LoggingFunction;
import org.morphix.lang.function.Predicates;
import org.morphix.reflection.Constructors;

/**
 * A utility class for logging API requests and responses, including success and error cases. This class provides
 * methods to log request details, response details, and exceptions in a structured format.
 * <p>
 * Request/response body logging is configurable through {@link ClientProperties.Logging}.
 * <p>
 * TODO: log headers on multiple lines for better readability.<br/>
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
	 * The string that will be displayed when {@link Logging.Mode#NONE} is used for request/response body logging.
	 */
	public static final String OMITTED = "<omitted>";

	/**
	 * The log message format for successful requests.
	 */
	private static final String LOG_MESSAGE_SUCCESS = Strings.EOL
			+ LOG_SEPARATOR
			+ Strings.EOL + "CLIENT: {}"
			+ Strings.EOL + "[REQUEST]"
			+ Strings.EOL + "METHOD: {}"
			+ Strings.EOL + "URL: {}"
			+ Strings.EOL + "PARAMETERS: {}"
			+ Strings.EOL + "HEADERS: {}"
			+ Strings.EOL + "BODY: {}"
			+ Strings.EOL + "[RESPONSE]"
			+ Strings.EOL + "STATUS: {}"
			+ Strings.EOL + "HEADERS: {}"
			+ Strings.EOL + "BODY: {}"
			+ Strings.EOL + "DURATION: {}s"
			+ Strings.EOL
			+ LOG_SEPARATOR;

	/**
	 * The log message format for failed requests.
	 */
	private static final String LOG_MESSAGE_ERROR = Strings.EOL
			+ LOG_SEPARATOR
			+ Strings.EOL + "CLIENT: {}"
			+ Strings.EOL + "[REQUEST]"
			+ Strings.EOL + "METHOD: {}"
			+ Strings.EOL + "URL: {}"
			+ Strings.EOL + "PARAMETERS: {}"
			+ Strings.EOL + "HEADERS: {}"
			+ Strings.EOL + "REQUEST BODY: {}"
			+ Strings.EOL + "[RESPONSE]"
			+ Strings.EOL + "STATUS: {}"
			+ Strings.EOL + "HEADERS: {}"
			+ Strings.EOL + "EXCEPTION: {}"
			+ Strings.EOL + "DURATION: {}s"
			+ Strings.EOL
			+ LOG_SEPARATOR;

	/**
	 * Logs all information for a successful HTTP request.
	 *
	 * @param <T> the type of the request
	 * @param <U> the type of the response
	 *
	 * @param loggingFunction the logging function used to output the log message
	 * @param apiClientClass the API class of the client making the request
	 * @param exchangeClient the exchange client that made the request
	 * @param apiRequest the API request object
	 * @param apiResponse the API response object
	 * @param duration the duration of the request
	 */
	public static <T, U> void logSuccess(
			final LoggingFunction loggingFunction,
			final Class<?> apiClientClass,
			final ExchangeClient exchangeClient,
			final ApiRequest<T> apiRequest,
			final ApiResponse<U> apiResponse,
			final Duration duration) {
		String logMessage = Messages.message(
				LOG_MESSAGE_SUCCESS,
				apiClientClass,
				apiRequest.getMethod(),
				apiRequest.getUrl(),
				exchangeClient.getDisplayParams(apiRequest),
				exchangeClient.getDisplayHeaders(apiRequest),
				describeBody(apiRequest, exchangeClient),
				Nullables.apply(apiResponse, ApiResponse::getStatus),
				Nullables.apply(apiResponse, exchangeClient::getDisplayHeaders),
				describeBody(apiResponse, exchangeClient),
				Temporals.toSeconds(duration.toMillis()));
		loggingFunction.log(logMessage);
	}

	/**
	 * Logs all information for a failed HTTP request.
	 *
	 * @param <T> the type of the request
	 * @param <U> the type of the response
	 *
	 * @param loggingFunction the logging function used to output the log message
	 * @param apiClientClass the API class of the client making the request
	 * @param exchangeClient the exchange client used for this request
	 * @param apiRequest the API request object
	 * @param apiResponse the API response object, if available
	 * @param duration the duration of the request
	 */
	public static <T, U> void logError(
			final LoggingFunction loggingFunction,
			final Class<?> apiClientClass,
			final ExchangeClient exchangeClient,
			final ApiRequest<T> apiRequest,
			final ApiResponse<U> apiResponse,
			final Duration duration) {
		Exception exception = Nullables.apply(apiResponse, ApiResponse::getException);
		String logMessage = Messages.message(
				LOG_MESSAGE_ERROR,
				apiClientClass,
				apiRequest.getMethod(),
				apiRequest.getUrl(),
				exchangeClient.getDisplayParams(apiRequest),
				exchangeClient.getDisplayHeaders(apiRequest),
				describeBody(apiRequest, exchangeClient),
				Nullables.apply(apiResponse, ApiResponse::getStatus),
				Nullables.apply(apiResponse, exchangeClient::getDisplayHeaders),
				exception,
				Temporals.toSeconds(duration.toMillis()));
		loggingFunction.log(logMessage);
		loggingFunction.log("{}", Nullables.apply(apiResponse, ApiResponse::getErrorMessage), exception);
	}

	/**
	 * Describes the body of a request or response based on the logging configuration of the exchange client.
	 *
	 * @param <T> the type of the body
	 *
	 * @param exchangeClient the exchange client used for this request
	 * @param apiMessage the API message containing the body to describe
	 * @return a string description of the body, or {@link #OMITTED} if body logging is disabled.
	 */
	protected static <T> String describeBody(final ApiMessage<T> apiMessage, final ExchangeClient exchangeClient) {
		if (null == apiMessage) {
			return null;
		}
		T body = apiMessage.getBody();
		if (null == body) {
			return null;
		}
		if (null == exchangeClient) {
			return body.toString();
		}
		ClientProperties clientProperties = exchangeClient.getClientProperties();
		ClientProperties.Logging loggingProperties = Nullables.apply(clientProperties, ClientProperties::getLogging);

		ClientProperties.Logging.Category bodyCategory = Nullables.apply(loggingProperties, ClientProperties.Logging::getBody);
		Logging.Mode bodyLoggingMode = Nullables.apply(bodyCategory, ClientProperties.Logging.Category::getMode);
		bodyLoggingMode = Nullables.nonNullOrDefault(bodyLoggingMode, Logging.Mode.FULL);

		if (Logging.Mode.NONE == bodyLoggingMode) {
			return OMITTED;
		}
		Boolean redact = Nullables.apply(bodyCategory, ClientProperties.Logging.Category::getRedact);
		if (Boolean.TRUE.equals(redact)) {
			Predicate<T> isSensitive = Nullables.nonNullOrDefault(exchangeClient.isSensitiveBody(), Predicates.alwaysFalse());
			if (isSensitive.test(body)) {
				return Sensitive.Value.REDACTED;
			}
		}
		if (Logging.Mode.FULL == bodyLoggingMode) {
			return body.toString();
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
