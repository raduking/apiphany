package org.apiphany.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.apiphany.ApiMessage;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.header.HeaderValues;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.Sensitive;
import org.morphix.lang.collections.Maps;
import org.morphix.lang.function.Predicates;

/**
 * Interface for exchange clients.
 *
 * @author Radu Sebastian LAZIN
 */
public interface ExchangeClient extends AutoCloseable {

	/**
	 * Basic rest template like exchange method.
	 *
	 * @param <T> request body type
	 * @param <U> response body type
	 *
	 * @param apiRequest request properties
	 * @return response
	 */
	<T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest);

	/**
	 * Returns the client properties, can be {@code null} if no properties are set.
	 *
	 * @param <T> client properties type
	 *
	 * @return the client properties
	 */
	default <T extends ClientProperties> T getClientProperties() {
		return null;
	}

	/**
	 * Basic rest template like async exchange method.
	 *
	 * @param <T> request/response body type
	 * @param <U> response body type
	 *
	 * @param apiRequest request properties
	 * @return response
	 */
	default <T, U> CompletableFuture<ApiResponse<U>> asyncExchange(final ApiRequest<T> apiRequest) {
		throw new UnsupportedOperationException("asyncExchange(ApiRequest)");
	}

	/**
	 * Returns the authentication type. By default, it returns {@link AuthenticationType#NONE}.
	 *
	 * @return the authentication type
	 */
	default AuthenticationType getAuthenticationType() {
		return AuthenticationType.NONE;
	}

	/**
	 * Returns the client name.
	 *
	 * @return the client name
	 */
	default String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Returns a predicate for the headers that should be redacted. By default, nothing is redacted.
	 *
	 * @return a predicate for the headers that should be redacted
	 */
	default Predicate<String> isSensitiveHeader() {
		return Predicates.alwaysFalse();
	}

	/**
	 * Returns a predicate for the parameters that should be redacted. By default, nothing is redacted.
	 *
	 * @return a predicate for the parameters that should be redacted
	 */
	default Predicate<String> isSensitiveParam() {
		return Predicates.alwaysFalse();
	}

	/**
	 * Returns common headers for all requests. By default, it returns an empty map. These headers will be added to each
	 * request made by this client.
	 *
	 * @return common headers for all requests
	 */
	default Map<String, List<String>> getCommonHeaders() {
		return Collections.emptyMap();
	}

	/**
	 * Returns tracing headers for all requests. By default, it returns an empty map. These headers will be added to each
	 * request made by this client.
	 *
	 * @return tracing headers for all requests
	 */
	default Map<String, List<String>> getTracingHeaders() {
		return Collections.emptyMap();
	}

	/**
	 * Returns all headers from the given {@link ApiMessage} that can be displayed. This method replaces sensitive headers
	 * with {@link HeaderValues#REDACTED}, useful when logging the headers of a message, and later implementations can
	 * override this method to show only the wanted headers.
	 *
	 * @param <T> the message body type
	 *
	 * @param apiMessage the API message containing the headers
	 * @return a map of headers with sensitive headers redacted
	 */
	default <T> Map<String, List<String>> getDisplayHeaders(final ApiMessage<T> apiMessage) {
		if (null == apiMessage) {
			return Collections.emptyMap();
		}
		Map<String, List<String>> headers = apiMessage.getHeaders();
		return Maps.replace(headers, isSensitiveHeader(), () -> Collections.singletonList(HeaderValues.REDACTED));
	}

	/**
	 * Returns all parameters from the given {@link ApiMessage} that can be displayed. This method replaces sensitive
	 * parameters with {@link HeaderValues#REDACTED}, useful when logging the parameters of a message, and later
	 * implementations can override this method to show only the wanted parameters.
	 *
	 * @param <T> the message body type
	 *
	 * @param apiRequest the API request containing the parameters
	 * @return a map of parameters with sensitive parameters redacted
	 */
	default <T> Map<String, List<String>> getDisplayParams(final ApiRequest<T> apiRequest) {
		if (null == apiRequest) {
			return Collections.emptyMap();
		}
		Map<String, List<String>> params = apiRequest.getParams();
		return Maps.replace(params, isSensitiveParam(), () -> Collections.singletonList(Sensitive.Value.REDACTED));
	}

	/**
	 * Returns custom properties of the client. If no custom properties are set, it returns {@code null}.
	 *
	 * @param <T> the type of the custom properties
	 *
	 * @param propertiesClass the class of the custom properties
	 * @return custom properties of the client
	 * @see ClientProperties#getCustomProperties(Class)
	 */
	default <T> T getCustomProperties(final Class<T> propertiesClass) {
		ClientProperties properties = getClientProperties();
		if (null == properties) {
			return null;
		}
		return properties.getCustomProperties(propertiesClass);
	}

	/**
	 * Returns the underlying exchange client cast to the given class if possible or throws an
	 * {@link IllegalArgumentException} if the underlying exchange client cannot be cast to the given class.
	 *
	 * @param <T> exchange client class type
	 *
	 * @param exchangeClientClass exchange client class
	 * @return the exchange client cast to the given class
	 * @throws IllegalArgumentException if the underlying exchange client cannot be cast to the given class
	 */
	default <T extends ExchangeClient> T as(final Class<T> exchangeClientClass) {
		if (!exchangeClientClass.isAssignableFrom(getClass())) {
			throw new IllegalArgumentException("The underlying exchange client cannot be cast to: " + exchangeClientClass);
		}
		return exchangeClientClass.cast(this);
	}

	/**
	 * Returns an exchange client builder.
	 *
	 * @return an exchange client builder
	 */
	static ExchangeClientBuilder builder() {
		return ExchangeClientBuilder.create();
	}

	/**
	 * Requires that the given exchange client has an authentication type set, otherwise throws an
	 * {@link IllegalStateException}.
	 *
	 * @param exchangeClient the exchange client
	 * @return the authentication type of the exchange client
	 * @throws IllegalStateException if the exchange client has no authentication type set
	 */
	static AuthenticationType requireAuthenticationType(final ExchangeClient exchangeClient) {
		AuthenticationType authenticationType = exchangeClient.getAuthenticationType();
		if (null == authenticationType) {
			throw new IllegalStateException("ExchangeClient: [" + exchangeClient.getName() + "]"
					+ " has no " + AuthenticationType.class.getSimpleName() + " set");
		}
		return authenticationType;
	}
}
