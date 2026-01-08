package org.apiphany.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.apiphany.lang.ScopedResource;
import org.apiphany.security.oauth2.client.OAuth2ExchangeClientBuilder;
import org.morphix.lang.Nullables;
import org.morphix.lang.function.Consumers;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;

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
	public ScopedResource<ExchangeClient> build() {
		if (null != this.exchangeClient && null != this.exchangeClientClass) {
			throw new IllegalStateException("Cannot set both exchange client instance and exchange client class");
		}
		boolean managed = exchangeClient == null;
		ExchangeClient client = managed ? build(exchangeClientClass, clientProperties) : exchangeClient;
		return ScopedResource.of(client, managed);
	}

	/**
	 * Builds an exchange client based on the client class and client properties. The exchange client class must have a
	 * constructor with one parameter of type {@link ClientProperties}.
	 *
	 * @param clientClass exchange client class
	 * @param clientProperties client properties object
	 * @return new exchange client
	 */
	protected static ExchangeClient build(final Class<? extends ExchangeClient> clientClass, final ClientProperties clientProperties) {
		Constructor<? extends ExchangeClient> constructor = Constructors.getDeclared(clientClass, ClientProperties.class);
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
	 * Copies all fields from another builder.
	 *
	 * @param exchangeClientBuilder the source builder
	 * @return this
	 */
	public ExchangeClientBuilder builder(final ExchangeClientBuilder exchangeClientBuilder) {
		return client(exchangeClientBuilder.exchangeClient)
				.client(exchangeClientBuilder.exchangeClientClass)
				.properties(exchangeClientBuilder.clientProperties);
	}

	/**
	 * Decorates this builder with another decorating builder.
	 *
	 * @param <T> decorating builder type
	 * @param decoratingBuilderClass decorating builder class
	 * @param decoratorCustomizer decorator customizer
	 * @return new decorating exchange client builder
	 */
	public <T extends ExchangeClientBuilder> T with(final Class<T> decoratingBuilderClass, final Consumer<T> decoratorCustomizer) {
		Method createMethod = Methods.Safe.getOneDeclared("create", decoratingBuilderClass);
		T decoratorBuilder = Methods.IgnoreAccess.invoke(createMethod, null);
		decoratorBuilder.builder(this);
		decoratorCustomizer.accept(decoratorBuilder);
		return decoratorBuilder;
	}

	/**
	 * Adds OAuth2 functionality.
	 *
	 * @param oAuth2BuilderCustomizer OAuth2 customizer
	 * @return new OAuth2 exchange client builder
	 */
	public OAuth2ExchangeClientBuilder oAuth2(final Consumer<OAuth2ExchangeClientBuilder> oAuth2BuilderCustomizer) {
		return with(OAuth2ExchangeClientBuilder.class, oAuth2BuilderCustomizer);
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
