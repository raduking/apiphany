package org.apiphany.client;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.apiphany.ApiMessage;
import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.header.HeaderValues;
import org.apiphany.lang.collections.Maps;
import org.apiphany.security.AuthenticationType;
import org.morphix.lang.Nullables;
import org.morphix.reflection.Constructors;

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
		throw new UnsupportedOperationException();
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
		return getClass().getName();
	}

	/**
	 * Returns all headers as {@link String}. This method helps log the headers of a message and subsequent implementations
	 * can override this method to show only the wanted headers.
	 *
	 * @param <T> the message body type
	 *
	 * @param apiMessage the API message containing the headers
	 * @return string representation of the message headers
	 */
	default <T> String getHeadersAsString(final ApiMessage<T> apiMessage) {
		return Maps.safe(apiMessage.getHeaders()).entrySet().stream().map(entry -> {
			String headerName = entry.getKey();
			List<String> headerValues = getRedactedHeaderPredicate().test(headerName)
					? Collections.singletonList(HeaderValues.REDACTED)
					: entry.getValue();
            return headerName + ":\"" + String.join(", ", headerValues) + "\"";
		}).toList().toString();
	}

	/**
	 * Returns a predicate for the headers that should be redacted. By default, nothing is redacted.
	 *
	 * @return a predicate for the headers that should be redacted
	 */
	default Predicate<String> getRedactedHeaderPredicate() {
		return s -> false;
	}

	/**
	 * Returns an exchange client builder.
	 *
	 * @return an exchange client builder
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * Exchange client builder.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class Builder {

		/**
		 * Exchange client class.
		 */
		private Class<? extends ExchangeClient> clientClass;

		/**
		 * Client properties.
		 */
		private ClientProperties clientProperties;

		/**
		 * Builds the exchange client based on the builder members.
		 *
		 * @return a new exchange client
		 */
		public ExchangeClient build() {
			Constructor<? extends ExchangeClient> constructor
					= Constructors.getDeclaredConstructor(clientClass, ClientProperties.class);
			ClientProperties properties = Nullables.nonNullOrDefault(clientProperties, ClientProperties::new);
			return Constructors.IgnoreAccess.newInstance(constructor, properties);
		}

		/**
		 * Sets the exchange client class.
		 *
		 * @param clientClass client class to set.
		 * @return this
		 */
		public Builder client(final Class<? extends ExchangeClient> clientClass) {
			this.clientClass = clientClass;
			return this;
		}

		/**
		 * Sets the client properties.
		 *
		 * @param clientProperties client properties to set
		 * @return this
		 */
		public Builder properties(final ClientProperties clientProperties) {
			this.clientProperties = clientProperties;
			return this;
		}

	}

}
