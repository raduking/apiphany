package org.apiphany.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apiphany.lang.ScopedResource;
import org.apiphany.security.client.SecuredExchangeClientBuilder;
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
	 * Decorator client classes. These are always managed by the builder.
	 */
	private final List<Class<? extends DecoratingExchangeClient>> decoratorClientClasses = new LinkedList<>();

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

		ScopedResource<ExchangeClient> scopedResource = ScopedResource.of(client, managed);
		for (Class<? extends DecoratingExchangeClient> decoratorClientClass : decoratorClientClasses) {
			Constructor<? extends DecoratingExchangeClient> constructor = Constructors.getDeclared(decoratorClientClass, ScopedResource.class);
			client = Constructors.IgnoreAccess.newInstance(constructor, scopedResource);
			scopedResource = ScopedResource.managed(client);
		}
		return scopedResource;
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
	 * <p>
	 * The decorating builder class must have a static {@code create} method that returns a new instance of the decorating
	 * builder.
	 *
	 * @param <T> decorating builder type
	 * @param decoratingBuilderClass decorating builder class
	 * @param decoratorCustomizer decorator customizer
	 * @return new decorating exchange client builder
	 */
	public <T extends ExchangeClientBuilder> T decoratedWithBuilder(final Class<T> decoratingBuilderClass, final Consumer<T> decoratorCustomizer) {
		Method createMethod = Methods.Safe.getOneDeclared("create", decoratingBuilderClass);
		T decoratorBuilder = Methods.IgnoreAccess.invoke(createMethod, null);
		decoratorBuilder.builder(this);
		decoratorCustomizer.accept(decoratorBuilder);
		return decoratorBuilder;
	}

	/**
	 * Decorates this builder with another decorating builder.
	 * <p>
	 * The decorating builder class must have a static {@code create} method that returns a new instance of the decorating
	 * builder.
	 *
	 * @param <T> decorating builder type
	 * @param decoratingBuilderClass decorating builder class
	 * @return new decorating exchange client builder
	 */
	public <T extends ExchangeClientBuilder> T decoratedWithBuilder(final Class<T> decoratingBuilderClass) {
		return decoratedWithBuilder(decoratingBuilderClass, Consumers.noConsumer());
	}

	/**
	 * Decorates this builder with another decorating exchange client.
	 * <p>
	 * The decorating exchange client class must have a constructor with one parameter of type {@link ExchangeClient} or
	 * {@link ScopedResource} depending on whether this builder has an exchange client class set or not.
	 *
	 * @param <T> decorating exchange client type
	 * @param decoratingClientClass decorating exchange client class
	 * @return new decorating exchange client builder
	 */
	public <T extends DecoratingExchangeClient> ExchangeClientBuilder decoratedWith(final Class<T> decoratingClientClass) {
		this.decoratorClientClasses.add(decoratingClientClass);
		return this;
	}

	/**
	 * Adds security functionality.
	 *
	 * @return new secured exchange client builder
	 */
	public SecuredExchangeClientBuilder securedWith() {
		return decoratedWithBuilder(SecuredExchangeClientBuilder.class);
	}
}
