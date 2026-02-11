package org.apiphany.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.lang.Lifecycle;
import org.apiphany.lang.Require;
import org.apiphany.lang.ScopedResource;
import org.apiphany.security.client.SecuredExchangeClientBuilder;
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
	 * Delegate exchange client builder. This is used when decorating builders and has priority over the other fields.
	 */
	protected ExchangeClientBuilder delegate;

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
		if (null != this.delegate) {
			return delegate.build();
		}
		if (null != this.exchangeClient && null != this.exchangeClientClass) {
			throw new IllegalStateException("Cannot set both exchange client instance and exchange client class");
		}
		if (null == this.exchangeClient && null == this.exchangeClientClass) {
			throw new IllegalStateException("Either exchange client instance or exchange client class must be set");
		}
		boolean managed = exchangeClient == null;
		ExchangeClient client = managed ? build(exchangeClientClass) : exchangeClient;

		ScopedResource<ExchangeClient> scopedResource = ScopedResource.of(client, Lifecycle.from(managed));
		for (Class<? extends DecoratingExchangeClient> decoratorClientClass : decoratorClientClasses) {
			client = build(scopedResource, decoratorClientClass);
			scopedResource = ScopedResource.managed(client);
		}
		return scopedResource;
	}

	/**
	 * Builds a decorating exchange client based on the scoped resource and decorating client class. The decorating exchange
	 * client class must have a constructor with one parameter of type {@link ScopedResource} if the scoped resource is
	 * managed or a constructor with one parameter of type {@link ExchangeClient} or {@link ScopedResource} if the scoped
	 * resource is unmanaged.
	 *
	 * @param scopedResource scoped resource exchange client
	 * @param decoratorClientClass decorating exchange client class
	 * @return new decorating exchange client
	 */
	@SuppressWarnings("resource")
	protected static ExchangeClient build(final ScopedResource<ExchangeClient> scopedResource,
			final Class<? extends DecoratingExchangeClient> decoratorClientClass) {
		Constructor<? extends DecoratingExchangeClient> resourceConstructor =
				Constructors.Safe.getDeclared(decoratorClientClass, ScopedResource.class);
		if (scopedResource.isManaged()) {
			Require.that(null != resourceConstructor, IllegalStateException::new,
					"Decorating exchange client class {} must have a constructor with one parameter of type {}",
					decoratorClientClass.getName(), ScopedResource.class.getName());
			return Constructors.IgnoreAccess.newInstance(resourceConstructor, scopedResource);
		}
		// this can happen only when the initial client is unmanaged
		Constructor<? extends DecoratingExchangeClient> clientConstructor = null;
		if (null == resourceConstructor) {
			clientConstructor = Constructors.Safe.getDeclared(decoratorClientClass, ExchangeClient.class);
			Require.that(null != clientConstructor, IllegalStateException::new,
					"Decorating exchange client class {} must have a constructor with one parameter of type {} or {}",
					decoratorClientClass.getName(), ExchangeClient.class.getName(), ScopedResource.class.getName());
		}
		return null != resourceConstructor
				? Constructors.IgnoreAccess.newInstance(resourceConstructor, scopedResource)
				: Constructors.IgnoreAccess.newInstance(clientConstructor, scopedResource.unwrap());
	}

	/**
	 * Builds an exchange client based on the client class and client properties. The exchange client class must have either
	 * a default constructor or a constructor with one parameter of type {@link ClientProperties}.
	 *
	 * @param clientClass exchange client class
	 * @return new exchange client
	 */
	protected ExchangeClient build(final Class<? extends ExchangeClient> clientClass) {
		if (null != clientProperties) {
			return build(clientClass, clientProperties);
		}
		Constructor<? extends ExchangeClient> constructor = Constructors.Safe.getDefault(clientClass);
		Require.that(null != constructor, IllegalStateException::new,
				"When client properties are not set exchange client class {} must have a default constructor", clientClass.getName());
		return Constructors.IgnoreAccess.newInstance(constructor);
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
		Require.that(null != constructor, IllegalStateException::new,
				"When client properties are not set exchange client class {} must not have a constructor with one parameter of type {}",
				clientClass.getName(), ClientProperties.class.getName());
		return Constructors.IgnoreAccess.newInstance(constructor, clientProperties);
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
	 * Sets the default exchange client class ({@link JavaNetHttpExchangeClient}).
	 *
	 * @return this
	 */
	public ExchangeClientBuilder withDefaultClient() {
		return client(JavaNetHttpExchangeClient.class);
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
	 * Sets the delegate exchange client builder.
	 *
	 * @param delegate delegate exchange client builder
	 * @return this
	 */
	public ExchangeClientBuilder delegate(final ExchangeClientBuilder delegate) {
		this.delegate = delegate;
		return this;
	}

	/**
	 * Decorates this builder with another decorating builder.
	 * <p>
	 * The decorating builder class must have a static {@code create} method that returns a new instance of the decorating
	 * builder.
	 * <p>
	 * The decorating builder is actually a new instance copies this builder and then applies the given customizer.
	 *
	 * @param <T> decorating builder type
	 * @param decoratingBuilderClass decorating builder class
	 * @param decoratorCustomizer decorator customizer
	 * @return new decorating exchange client builder
	 */
	public <T extends ExchangeClientBuilder> T decoratedWithBuilder(final Class<T> decoratingBuilderClass, final Consumer<T> decoratorCustomizer) {
		Method createMethod = Methods.Safe.getOneDeclared("create", decoratingBuilderClass);
		T decoratorBuilder = Methods.IgnoreAccess.invoke(createMethod, null);
		decoratorBuilder.delegate(this);
		decoratorBuilder.properties(clientProperties);
		decoratorCustomizer.accept(decoratorBuilder);
		return decoratorBuilder;
	}

	/**
	 * Decorates this builder with another decorating builder.
	 * <p>
	 * The decorating builder class must have a static {@code create} method that returns a new instance of the decorating
	 * builder.
	 * <p>
	 * The decorating builder is actually a new instance copies this builder and then applies the given customizer.
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
