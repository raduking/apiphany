package org.apiphany.client;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

import org.apiphany.lang.Pair;
import org.apiphany.security.oauth2.client.OAuth2ExchangeClientBuilder;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.Consumers;
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
	private Class<? extends ExchangeClient> exchangeClientClass;

	/**
	 * Exchange client for which the caller will manage the life cycle.
	 */
	private ExchangeClient exchangeClient;

	/**
	 * Client properties.
	 */
	protected ClientProperties clientProperties;

	/**
	 * Hide constructor.
	 */
	protected ExchangeClientBuilder() {
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
	 * @return a new exchange client pair with life cycle management information
	 */
	@SuppressWarnings("resource")
	public Pair<ExchangeClient, Boolean> build() {
		if (null != exchangeClient) {
			return Pair.of(exchangeClient, false);
		}
		return Pair.of(build(exchangeClientClass, clientProperties), true);
	}

	/**
	 * Builds an exchange client based on the client class and client properties. The exchange client class must have a
	 * constructor with one parameter of type {@link ClientProperties}.
	 *
	 * @param clientClass exchange client class
	 * @param clientProperties client properties object
	 * @return new exchange client
	 */
	public static ExchangeClient build(final Class<? extends ExchangeClient> clientClass, final ClientProperties clientProperties) {
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
		this.exchangeClientClass = clientClass;
		return this;
	}

	/**
	 * Sets the exchange client which will be managed by the caller.
	 *
	 * @param client client to set.
	 * @return this
	 */
	public ExchangeClientBuilder client(final ExchangeClient client) {
		this.exchangeClient = client;
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

	/**
	 * Adds OAuth2 functionality.
	 *
	 * @param oAuth2BuilderCustomizer OAuth2 customizer
	 * @return new OAuth2 exchange client builder
	 */
	public OAuth2ExchangeClientBuilder oAuth2(final Consumer<OAuth2ExchangeClientBuilder> oAuth2BuilderCustomizer) {
		OAuth2ExchangeClientBuilder oAuth2ExchangeClientBuilder = OAuth2ExchangeClientBuilder.create();
		oAuth2ExchangeClientBuilder
				.client(exchangeClientClass)
				.client(exchangeClient)
				.properties(clientProperties);
		oAuth2BuilderCustomizer.accept(oAuth2ExchangeClientBuilder);
		return oAuth2ExchangeClientBuilder;
	}

	/**
	 * Adds OAuth2 functionality.
	 *
	 * @return new OAuth2 exchange client builder
	 */
	public OAuth2ExchangeClientBuilder oAuth2() {
		return oAuth2(Consumers.noConsumer());
	}
}
