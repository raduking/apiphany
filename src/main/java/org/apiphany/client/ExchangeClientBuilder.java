package org.apiphany.client;

import java.lang.reflect.Constructor;

import org.morphix.lang.Nullables;
import org.morphix.reflection.Constructors;

/**
 * Exchange client builder.
 *
 * @author Radu Sebastian LAZIN
 */
public class ExchangeClientBuilder {

	/**
	 * Exchange client class.
	 */
	private Class<? extends ExchangeClient> clientClass;

	/**
	 * Client properties.
	 */
	private ClientProperties clientProperties;

	/**
	 * Initialized with this.
	 */
	private ExchangeClientBuilder proxy;

	/**
	 * Hide constructor.
	 */
	private ExchangeClientBuilder() {
		// empty
	}

	/**
	 * Creates an exchange client builder.
	 *
	 * @return the exchange client builder
	 */
	public static ExchangeClientBuilder create() {
		return new ExchangeClientBuilder();
	}

	/**
	 * Builds the exchange client based on the builder members.
	 *
	 * @return a new exchange client
	 */
	public ExchangeClient build() {
		if (null != proxy) {
			return proxy.build();
		}
		Constructor<? extends ExchangeClient> constructor = Constructors.getDeclaredConstructor(clientClass, ClientProperties.class);
		ClientProperties properties = Nullables.nonNullOrDefault(clientProperties, ClientProperties::new);
		return Constructors.IgnoreAccess.newInstance(constructor, properties);
	}

	/**
	 * Sets the exchange client class.
	 *
	 * @param clientClass client class to set.
	 * @return this
	 */
	public ExchangeClientBuilder client(final Class<? extends ExchangeClient> clientClass) {
		this.clientClass = clientClass;
		return this;
	}

	/**
	 * Sets the client properties.
	 *
	 * @param clientProperties client properties to set
	 * @return this
	 */
	public ExchangeClientBuilder properties(final ClientProperties clientProperties) {
		this.clientProperties = clientProperties;
		return this;
	}

}
