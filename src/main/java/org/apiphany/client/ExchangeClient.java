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
import org.apiphany.lang.collections.Maps;
import org.apiphany.security.AuthenticationType;
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
	 * Returns the client properties.
	 *
	 * @param <T> client properties type
	 *
	 * @return the client properties
	 */
	default <T extends ClientProperties> T getClientProperties() {
		throw new UnsupportedOperationException("getClientProperties");
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
	 * Returns all headers as {@link String}. This method helps log the headers of a message, and later implementations can
	 * override this method to show only the wanted headers.
	 *
	 * @param <T> the message body type
	 *
	 * @param apiMessage the API message containing the headers
	 * @return string representation of the message headers
	 */
	default <T> String getHeadersAsString(final ApiMessage<T> apiMessage) {
		return Maps.safe(apiMessage.getHeaders()).entrySet().stream().map(entry -> {
			String headerName = entry.getKey();
			List<String> headerValues = isSensitiveHeader().test(headerName)
					? Collections.singletonList(HeaderValues.REDACTED)
					: entry.getValue();
			return HeaderValues.value(headerName, headerValues, ":");
		}).toList().toString();
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
	 * Returns an exchange client builder.
	 *
	 * @return an exchange client builder
	 */
	static ExchangeClientBuilder builder() {
		return ExchangeClientBuilder.create();
	}
}
