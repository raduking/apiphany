package org.apiphany.client;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.lang.Require;
import org.apiphany.logging.Slf4jLoggerAdapter;
import org.apiphany.security.client.SecuredExchangeClientBuilder;
import org.morphix.lang.Unchecked;
import org.morphix.lang.collections.Lists;
import org.morphix.lang.function.Consumers;
import org.morphix.lang.function.LoggerAdapter;
import org.morphix.lang.resource.ScopedResource;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Methods;

/**
 * Exchange client builder that can be used to build exchange clients in a flexible way. It supports:
 *
 * <ul>
 * <li>building exchange clients based on an exchange client class or an exchange client instance.</li>
 * <li>decorating exchange clients with decorating exchange client classes or decorating exchange client builders.</li>
 * <li>delegating the building process to another builder.</li>
 * <li>client properties and client arguments that can be used when the exchange client class has a constructor with
 * parameters.</li>
 * </ul>
 *
 * @author Radu Sebastian LAZIN
 */
public class ExchangeClientBuilder {

	/**
	 * The logger object.
	 */
	private static final LoggerAdapter LOGGER = Slf4jLoggerAdapter.of(ExchangeClientBuilder.class);

	/**
	 * Delegate exchange client builder. This is used when decorating builders and has priority over the other fields.
	 */
	protected ExchangeClientBuilder delegate;

	/**
	 * Exchange client class.
	 */
	private Class<? extends ExchangeClient> exchangeClientClass;

	/**
	 * Exchange client with life cycle management information.
	 */
	private ScopedResource<ExchangeClient> exchangeClientResource;

	/**
	 * Client properties.
	 */
	protected ClientProperties clientProperties;

	/**
	 * Client arguments. These are used when the exchange client class has a constructor with parameters different than
	 * {@link ClientProperties}. The client arguments must be in the same order as the constructor parameters and the
	 * exchange client class must have only one constructor with the same number of parameters as the client arguments.
	 */
	private List<Object> clientArguments = new LinkedList<>();

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
	public ScopedResource<ExchangeClient> build() {
		return build(Unchecked.Undeclared::reThrow);
	}

	/**
	 * Builds the exchange client based on the builder members.
	 *
	 * @param buildErrorHandler error handler to be called if an exception occurs during the build process
	 * @return a new exchange client resource with life cycle management information
	 */
	@SuppressWarnings("resource")
	protected ScopedResource<ExchangeClient> build(final Consumer<Exception> buildErrorHandler) {
		ScopedResource<ExchangeClient> scopedResource = buildMainClient(buildErrorHandler);
		if (null == scopedResource) {
			return null;
		}
		try {
			for (Class<? extends DecoratingExchangeClient> decoratorClientClass : decoratorClientClasses) {
				ExchangeClient decoratorClient = build(scopedResource, decoratorClientClass);
				scopedResource = ScopedResource.managed(decoratorClient);
			}
		} catch (Exception e) {
			String exchangeClientName = scopedResource.unwrap().getName();
			scopedResource.closeIfManaged(
					closeException -> LOGGER.warn("Error closing exchange client {} on build error",
							exchangeClientName, closeException));
			buildErrorHandler.accept(e);
		}
		return scopedResource;
	}

	/**
	 * Builds the main exchange client based on the exchange client class or exchange client instance. If both are set an
	 * exception is thrown. If none of them is set an exception is thrown.
	 *
	 * @param buildErrorHandler error handler to be called if an exception occurs during the build process
	 * @return a new exchange client resource with life cycle management information
	 */
	@SuppressWarnings("resource")
	protected ScopedResource<ExchangeClient> buildMainClient(final Consumer<Exception> buildErrorHandler) {
		Map<String, Object> fieldsMap = getExclusiveRequiredFields();
		long nonNullFields = fieldsMap.values().stream().filter(Objects::nonNull).count();
		try {
			requireThat(1 == nonNullFields, "One and only one of the following fields must be set: {}", fieldsMap.keySet());
			if (null != delegate) {
				return delegate.build();
			}
			if (null != exchangeClientResource) {
				return exchangeClientResource;
			}
			ExchangeClient exchangeClient = build(exchangeClientClass);
			return ScopedResource.managed(exchangeClient);
		} catch (Exception e) {
			buildErrorHandler.accept(e);
			return null;
		}
	}

	/**
	 * Returns a map of the exclusive required fields. These are the fields of the builder of which only one can be set. The
	 * returned map is an ordered unmodifiable map to keep the field order in case of error messages.
	 *
	 * @return a map of the exclusive required fields
	 */
	public Map<String, Object> getExclusiveRequiredFields() {
		Map<String, Object> fieldsMap = new LinkedHashMap<>();
		fieldsMap.put("delegate", delegate);
		fieldsMap.put("exchangeClientClass", exchangeClientClass);
		fieldsMap.put("exchangeClientResource", exchangeClientResource);
		return Collections.unmodifiableMap(fieldsMap);
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
		String exchangeClientClassName = ExchangeClient.class.getName();
		if (scopedResource.isManaged()) {
			requireThat(null != resourceConstructor,
					"Decorating exchange client class: {}, must have a constructor with one parameter of type: {}<{}>",
					decoratorClientClass.getName(), ScopedResource.class.getName(), exchangeClientClassName);
			return Constructors.IgnoreAccess.newInstance(resourceConstructor, scopedResource);
		}
		// this can happen only when the initial client is unmanaged
		Constructor<? extends DecoratingExchangeClient> clientConstructor = null;
		if (null == resourceConstructor) {
			clientConstructor = Constructors.Safe.getDeclared(decoratorClientClass, ExchangeClient.class);
			requireThat(null != clientConstructor,
					"Decorating exchange client class: {}, must have a constructor with one parameter of type: {}, or type: {}<{}>",
					decoratorClientClass.getName(), exchangeClientClassName, ScopedResource.class.getName(), exchangeClientClassName);
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
			return build(clientClass, clientProperties, clientArguments);
		}
		Constructor<? extends ExchangeClient> constructor = Constructors.Safe.getDefault(clientClass);
		requireThat(null != constructor,
				"When client properties are not set exchange client class: {}, must have a default constructor", clientClass.getName());
		return Constructors.IgnoreAccess.newInstance(constructor);
	}

	/**
	 * Builds an exchange client based on the client class and client properties and client arguments. The exchange client
	 * class must have a constructor with one parameter of type {@link ClientProperties} and extra parameters with the same
	 * types and in the same order as the client arguments.
	 *
	 * @param clientClass exchange client class
	 * @param clientProperties client properties object
	 * @param clientArguments client arguments
	 * @return new exchange client
	 */
	protected static ExchangeClient build(final Class<? extends ExchangeClient> clientClass, final ClientProperties clientProperties,
			final List<Object> clientArguments) {
		if (Lists.isEmpty(clientArguments)) {
			return build(clientClass, clientProperties);
		}
		Class<?>[] parameterTypes = new Class<?>[clientArguments.size() + 1];
		parameterTypes[0] = ClientProperties.class;
		for (int i = 0; i < clientArguments.size(); ++i) {
			parameterTypes[i + 1] = clientArguments.get(i).getClass();
		}
		Object[] arguments = new Object[clientArguments.size() + 1];
		arguments[0] = clientProperties;
		for (int i = 0; i < clientArguments.size(); ++i) {
			Object argument = clientArguments.get(i);
			arguments[i + 1] = argument;
		}
		Constructor<? extends ExchangeClient> constructor = Constructors.Safe.findOneMatching(clientClass, parameterTypes);
		requireThat(null != constructor,
				"Client class: {}, must have a constructor matching the client arguments provided in the builder: {}",
				clientClass.getName(), List.of(parameterTypes));
		return Constructors.IgnoreAccess.newInstance(constructor, arguments);
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
		Constructor<? extends ExchangeClient> constructor = Constructors.Safe.getDeclared(clientClass, ClientProperties.class);
		requireThat(null != constructor,
				"When client or SSL properties are set exchange client class: {}, must have a constructor with one parameter of type: {}",
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
	@SuppressWarnings("resource")
	public ExchangeClientBuilder client(final ExchangeClient client) {
		return client(ScopedResource.unmanaged(client))
				.properties(client.getClientProperties());
	}

	/**
	 * Sets the exchange client with life cycle management information.
	 *
	 * @param clientResource client resource to set
	 * @return this
	 */
	public ExchangeClientBuilder client(final ScopedResource<ExchangeClient> clientResource) {
		this.exchangeClientResource = clientResource;
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
		if (null != delegate) {
			delegate.properties(clientProperties);
		}
		return this;
	}

	/**
	 * Adds a client argument. Client arguments are used when the exchange client class has a constructor with parameters
	 * different than {@link ClientProperties}. The client arguments must be in the same order as the constructor parameters
	 * and the exchange client class must have only one constructor with the same number of parameters as the client
	 * arguments.
	 * <p>
	 * Client arguments cannot be set if the exchange client class is not set because the client arguments are used to find
	 * the constructor of the exchange client class and if the exchange client class is not set the client arguments cannot
	 * be used.
	 *
	 * @param argument client argument to add
	 * @return this
	 * @throws IllegalStateException if the exchange client class is not set
	 */
	public ExchangeClientBuilder argument(final Object argument) {
		requireThat(null != argument, "Client argument must not be null");
		requireThat(null != exchangeClientClass,
				"Client argument cannot be set in builder when client class is not set (client class is required to use client arguments)");
		this.clientArguments.add(argument);
		return this;
	}

	/**
	 * Adds client arguments. Client arguments are used when the exchange client class has a constructor with parameters
	 * different than {@link ClientProperties}. The client arguments must be in the same order as the constructor parameters
	 * and the exchange client class must have only one constructor with the same number of parameters as the client
	 * arguments.
	 * <p>
	 * Client arguments cannot be set if the exchange client class is not set because the client arguments are used to find
	 * the constructor of the exchange client class and if the exchange client class is not set the client arguments cannot
	 * be used.
	 *
	 * @param arguments client arguments to add
	 * @return this
	 * @throws IllegalStateException if the exchange client class is not set
	 */
	public ExchangeClientBuilder arguments(final List<Object> arguments) {
		for (Object clientArgument : arguments) {
			argument(clientArgument);
		}
		return this;
	}

	/**
	 * Adds client arguments. Client arguments are used when the exchange client class has a constructor with parameters
	 * different than {@link ClientProperties}. The client arguments must be in the same order as the constructor parameters
	 * and the exchange client class must have only one constructor with the same number of parameters as the client
	 * arguments.
	 * <p>
	 * Client arguments cannot be set if the exchange client class is not set because the client arguments are used to find
	 * the constructor of the exchange client class and if the exchange client class is not set the client arguments cannot
	 * be used.
	 *
	 * @param clientArguments client arguments to add
	 * @return this
	 * @throws IllegalStateException if the exchange client class is not set
	 */
	public ExchangeClientBuilder arguments(final Object... clientArguments) {
		return arguments(Arrays.asList(clientArguments));
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
	 * builder. The decorating builder is a new instance copies this builders client properties and then applies the given
	 * customizer.
	 *
	 * @param <T> decorating builder type
	 *
	 * @param decoratingBuilderClass decorating builder class
	 * @param decoratorCustomizer decorator customizer
	 * @return new decorating exchange client builder
	 */
	public <T extends ExchangeClientBuilder> T decoratedWithBuilder(final Class<T> decoratingBuilderClass, final Consumer<T> decoratorCustomizer) {
		Method createMethod = Methods.Safe.getOneDeclared("create", decoratingBuilderClass);
		requireThat(null != createMethod,
				"Decorating builder class {} must have a static create method with no parameters returning an instance of the builder",
				decoratingBuilderClass.getName());
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
	 * builder. The decorating builder is a new instance copies this builders client properties and then applies the given
	 * customizer.
	 *
	 * @param <T> decorating builder type
	 *
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
	 *
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

	/**
	 * Short hand for {@code Require.that(condition, IllegalStateException::new, messageTemplate, messageArgs)}.
	 *
	 * @param condition the condition to check
	 * @param messageTemplate the error message template
	 * @param messageArgs the error message arguments
	 * @see Require#that(boolean, java.util.function.Function, String, Object...)
	 */
	protected static void requireThat(final boolean condition, final String messageTemplate, final Object... messageArgs) {
		Require.that(condition, IllegalStateException::new, messageTemplate, messageArgs);
	}

	/**
	 * Returns true if the builder uses an already built exchange client instance, false otherwise.
	 *
	 * @return true if the builder uses an already built exchange client instance, false otherwise
	 */
	protected boolean isBuiltClient() {
		if (null != exchangeClientResource) {
			return true;
		}
		if (null != delegate) {
			return delegate.isBuiltClient();
		}
		return false;
	}
}
