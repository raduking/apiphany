package org.apiphany;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.lang.ScopedResource;
import org.apiphany.lang.Strings;
import org.apiphany.lang.accumulator.DurationAccumulator;
import org.apiphany.lang.collections.JavaArrays;
import org.apiphany.lang.collections.Lists;
import org.apiphany.lang.retry.Retry;
import org.apiphany.meters.BasicMeters;
import org.apiphany.meters.MeterFactory;
import org.apiphany.security.AuthenticationType;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.lang.Unchecked;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.predicates.MemberPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic client for API calls.
 *
 * @author Radu Sebastian LAZIN
 */
public class ApiClient implements AutoCloseable {

	/**
	 * Class logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

	/**
	 * <code>"api"</code> string used in most APIs.
	 */
	public static final String API = "api";

	/**
	 * No base URL provided to be used when constructing the API client object.
	 */
	public static final String EMPTY_BASE_URL = "";

	/**
	 * The URL as string as the base path.
	 */
	private final String baseUrl;

	/**
	 * Normally, the client handles exceptions gracefully by logging the full exception and retransmitting the exception
	 * message to the caller.
	 * <p>
	 * Change this to <code><b>true</b></code> if you want the client to re-throw the caught exceptions (one reason, for
	 * example, would be to handle the exceptions differently in accordance with the needs of the application).
	 * <p>
	 * The default value is <code><b>false</b></code>.
	 */
	private boolean bleedExceptions = false;

	/**
	 * Retry for API calls. By default, no retry is configured. This object is present here so that the implementing client
	 * can set the same retry for all requests if needed.
	 */
	private Retry retry = Retry.NO_RETRY;

	/**
	 * Metrics enable/disable flag.
	 */
	private boolean metricsEnabled = true;

	/**
	 * Basic meters. By default, all metrics are empty. This object is present here so that the implementing client can set
	 * the same metrics for all requests if needed.
	 */
	private BasicMeters meters = BasicMeters.DEFAULT;

	/**
	 * The meter factory.
	 */
	private MeterFactory meterFactory;

	/**
	 * Exchange clients map based on the authentication type and a pair of exchange client and managed flag that tells the
	 * API client if the exchange client's life cycle is managed by the API client or not.
	 */
	private final Map<AuthenticationType, ScopedResource<ExchangeClient>> exchangeClientsMap = new ConcurrentHashMap<>();

	/**
	 * Constructor with exchange clients. Constructing the {@link ApiClient} with multiple exchange clients allows handling
	 * multiple authentication types in the same client.
	 * <p>
	 * Base URL is used as the root URL to which all paths will be appended when making requests.
	 * <ul>
	 * <li>If no base URL is specified, an empty base URL is used meaning that each request must provide the full URL.</li>
	 * <li>If a base URL is specified, all request paths will be appended to the base URL.</li>
	 * <li>If exchange clients provide the base URL in their properties they will override the base URL provided here for
	 * requests done with that client (or for that authentication type)</li>
	 * </ul>
	 *
	 * @param baseUrl base URL to which all paths will be appended
	 * @param exchangeClientsMap map of exchange clients with life cycle information based on authentication type
	 * @throws IllegalStateException if multiple exchange clients are provided for the same authentication type
	 */
	protected ApiClient(final String baseUrl, final Map<AuthenticationType, ScopedResource<ExchangeClient>> exchangeClientsMap) {
		LOGGER.debug("Initializing: {}(baseUrl: {})", getClass().getSimpleName(), Strings.isEmpty(baseUrl) ? "<no-base-url>" : baseUrl);
		this.baseUrl = baseUrl;
		this.exchangeClientsMap.putAll(exchangeClientsMap);

		initializeTypeObjects(this);
	}

	/**
	 * Constructor with a list of exchange clients with life cycle information. The {@link ApiClient} can have multiple
	 * exchange clients for different authentication types.
	 *
	 * @param baseUrl base URL to which all paths will be appended
	 * @param exchangeClients list of exchange clients
	 */
	protected ApiClient(final String baseUrl, final List<ScopedResource<ExchangeClient>> exchangeClients) {
		this(baseUrl, buildExchangeClientsMap(exchangeClients));
	}

	/**
	 * Constructor with a list of exchange clients with life cycle information. The {@link ApiClient} can have multiple
	 * exchange clients for different authentication types.
	 *
	 * @param exchangeClients list of exchange clients
	 */
	protected ApiClient(final List<ScopedResource<ExchangeClient>> exchangeClients) {
		this(EMPTY_BASE_URL, exchangeClients);
	}

	/**
	 * Constructor with base URL and a scoped exchange client.
	 *
	 * @param baseUrl base URL
	 * @param clientResource scoped exchange client
	 */
	protected ApiClient(final String baseUrl, final ScopedResource<ExchangeClient> clientResource) {
		this(baseUrl, Collections.singletonList(clientResource));
	}

	/**
	 * Constructor with a scoped exchange client and no base URL.
	 *
	 * @param clientResource scoped exchange client
	 */
	protected ApiClient(final ScopedResource<ExchangeClient> clientResource) {
		this(EMPTY_BASE_URL, clientResource);
	}

	/**
	 * Constructor with only one exchange client.
	 *
	 * @param baseUrl base URL to which all paths will be appended
	 * @param exchangeClient exchange client
	 */
	protected ApiClient(final String baseUrl, final ExchangeClient exchangeClient) {
		this(baseUrl, ScopedResource.unmanaged(exchangeClient));
	}

	/**
	 * Constructor with only one exchange client and no base URL.
	 *
	 * @param exchangeClient exchange client
	 */
	protected ApiClient(final ExchangeClient exchangeClient) {
		this(EMPTY_BASE_URL, exchangeClient);
	}

	/**
	 * Constructor with base URL and exchange client builder, the built exchange client will be managed by this client.
	 *
	 * @param baseUrl base URL
	 * @param exchangeClientBuilders exchange client builders
	 */
	protected ApiClient(final String baseUrl, final ExchangeClientBuilder... exchangeClientBuilders) {
		this(baseUrl, buildExchangeClientsList(exchangeClientBuilders));
	}

	/**
	 * Constructor with exchange client builder, this client will manage the built exchange client.
	 *
	 * @param exchangeClientBuilders exchange client builders
	 */
	protected ApiClient(final ExchangeClientBuilder... exchangeClientBuilders) {
		this(EMPTY_BASE_URL, exchangeClientBuilders);
	}

	/**
	 * Constructor with base URL and a default exchange client.
	 * <p>
	 * This constructor should be used only for quick tests or prototyping. For production code, it is recommended to use
	 * one of the other constructors that allow more fine-tuning of the exchange client.
	 *
	 * @param baseUrl base URL
	 */
	protected ApiClient(final String baseUrl) {
		this(baseUrl, withDefaultClient());
	}

	/**
	 * Builds the exchange clients map based on authentication type. This method ensures that there is only one exchange
	 * client per authentication type and, in case of error, closes all managed exchange clients.
	 *
	 * @param exchangeClientResources list of exchange client resources
	 * @return map of exchange clients based on authentication type
	 * @throws IllegalStateException if no exchange clients are provided
	 * @throws IllegalStateException if multiple exchange clients are provided for the same authentication type
	 */
	@SuppressWarnings("resource")
	private static Map<AuthenticationType, ScopedResource<ExchangeClient>> buildExchangeClientsMap(
			final List<ScopedResource<ExchangeClient>> exchangeClientResources) {
		if (Lists.isEmpty(exchangeClientResources)) {
			throw new IllegalStateException("At least one: " + ExchangeClient.class.getName()
					+ " must be provided to instantiate: " + ApiClient.class.getName());
		}
		Map<AuthenticationType, ScopedResource<ExchangeClient>> result = new LinkedHashMap<>();
		try {
			for (ScopedResource<ExchangeClient> exchangeClientResource : exchangeClientResources) {
				ExchangeClient newClient = exchangeClientResource.unwrap();
				AuthenticationType authenticationType = ExchangeClient.requireAuthenticationType(newClient);
				ScopedResource<ExchangeClient> existingResource = result.putIfAbsent(authenticationType, exchangeClientResource);
				if (null != existingResource) {
					throw duplicateException(authenticationType, existingResource.unwrap(), newClient);
				}
			}
			return Collections.unmodifiableMap(result);
		} catch (Exception e) {
			closeExchangeClients(exchangeClientResources);
			return Unchecked.reThrow(e);
		}
	}

	/**
	 * Builds a list of scoped exchange clients from the given exchange client builders.
	 * <p>
	 * If no exchange client builders are provided, a default exchange client is created.
	 *
	 * @param exchangeClientBuilders exchange client builders
	 * @return list of scoped exchange clients
	 */
	private static List<ScopedResource<ExchangeClient>> buildExchangeClientsList(final ExchangeClientBuilder... exchangeClientBuilders) {
		if (JavaArrays.isEmpty(exchangeClientBuilders)) {
			return List.of(withDefaultClient().build());
		}
		return List.of(exchangeClientBuilders)
				.stream()
				.map(ExchangeClientBuilder::build)
				.toList();
	}

	/**
	 * Builds an exception for the case when multiple exchange clients are provided for the same authentication type. The
	 * exception message contains the authentication type and the names of the existing and new exchange clients.
	 *
	 * @param authenticationType authentication type
	 * @param existingClient existing exchange client
	 * @param newClient new exchange client
	 * @return IllegalStateException to be thrown
	 */
	protected static IllegalStateException duplicateException(final AuthenticationType authenticationType,
			final ExchangeClient existingClient, final ExchangeClient newClient) {
		return new IllegalStateException("Failed to instantiate [" + ApiClient.class.getName() + "]."
				+ " Client entry for authentication type: [" + authenticationType + ", " + existingClient.getName() + "]"
				+ " already exists when trying to add client: [" + newClient.getName() + "]");
	}

	/**
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		closeExchangeClients(exchangeClientsMap.values());
	}

	/**
	 * Closes all managed exchange clients in the given collection.
	 *
	 * @param exchangeClients collection of scoped exchange clients
	 */
	@SuppressWarnings("resource")
	protected static void closeExchangeClients(final Collection<ScopedResource<ExchangeClient>> exchangeClients) {
		for (ScopedResource<ExchangeClient> scopedResource : exchangeClients) {
			String exchangeClientName = scopedResource.unwrap().getName();
			LOGGER.debug("Closing: [{}] for [{}:{}]", exchangeClientName, AuthenticationType.class.getSimpleName(), exchangeClientName);
			scopedResource.closeIfManaged(e -> LOGGER.error("Error closing: [{}]", exchangeClientName, e));
		}
	}

	/**
	 * Returns a new {@link ApiClient} object.
	 *
	 * @param baseUrl base URL
	 * @param exchangeClients exchange client objects
	 * @return a new ApiClient object
	 */
	public static ApiClient of(final String baseUrl, final List<ExchangeClient> exchangeClients) {
		return new ApiClient(baseUrl, Lists.safe(exchangeClients).stream().map(ScopedResource::unmanaged).toList());
	}

	/**
	 * Returns a new {@link ApiClient} object.
	 *
	 * @param baseUrl base URL
	 * @param exchangeClient exchange client object
	 * @return a new ApiClient object
	 */
	public static ApiClient of(final String baseUrl, final ExchangeClient exchangeClient) {
		return of(baseUrl, Collections.singletonList(exchangeClient));
	}

	/**
	 * Returns a new {@link ApiClient} object.
	 *
	 * @param baseUrl base URL
	 * @param exchangeClientBuilders exchange client objects builders
	 * @return a new ApiClient object
	 */
	public static ApiClient of(final String baseUrl, final ExchangeClientBuilder... exchangeClientBuilders) {
		return new ApiClient(baseUrl, exchangeClientBuilders);
	}

	/**
	 * Returns a new {@link ApiClient} object.
	 *
	 * @param exchangeClientBuilders exchange client objects builders
	 * @return a new ApiClient object
	 */
	public static ApiClient of(final ExchangeClientBuilder... exchangeClientBuilders) {
		return of(EMPTY_BASE_URL, exchangeClientBuilders);
	}

	/**
	 * Initializes the defined type objects in derived classes and initialized with {@link #typeObject()}.
	 *
	 * @param apiClient API Client
	 */
	private static void initializeTypeObjects(final ApiClient apiClient) {
		Predicate<Field> predicate = MemberPredicates.withAllModifiers(Modifier::isStatic, Modifier::isFinal);
		Fields.getAllDeclaredInHierarchy(apiClient.getClass(), predicate)
				.stream()
				.filter(field -> Objects.equals(field.getType(), GenericClass.class))
				.forEach(ApiClient::initializeTypeObject);
	}

	/**
	 * Initializes a type object defined in derived classes and initialized with {@link #typeObject()}.
	 *
	 * @param typeObjectField type object field.
	 */
	private static void initializeTypeObject(final Field typeObjectField) {
		GenericClass<?> typeObject = Fields.IgnoreAccess.get(null, typeObjectField);
		if (null == typeObject.getType()) {
			ParameterizedType parameterizedType = JavaObjects.cast(typeObjectField.getGenericType());
			Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
			if (actualTypeArgument instanceof ParameterizedType) {
				typeObject.setType(actualTypeArgument);
			} else {
				throw new IllegalArgumentException("The typeObject method should only be used for generic types, current type: "
						+ actualTypeArgument.getTypeName() + " is not a generic type for static field: "
						+ typeObjectField.getName());
			}
		}
	}

	/**
	 * Returns the base URL of the client.
	 *
	 * @return the base URL of the client
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Builds a {@link GenericClass} object to be used as a type in requests. The type objects must be declared with
	 * <code>static final</code> modifiers.
	 *
	 * @param <T> object type
	 * @return type object
	 */
	public static <T> GenericClass<T> typeObject() {
		return GenericClass.of();
	}

	/**
	 * Returns an {@link ApiClientFluentAdapter} for fluent syntax.
	 *
	 * @param exchangeClient exchange client
	 * @return API client adapter
	 */
	private ApiClientFluentAdapter client(final ExchangeClient exchangeClient) {
		return ApiClientFluentAdapter.of(this)
				.exchangeClient(exchangeClient);
	}

	/**
	 * Returns an {@link ApiClientFluentAdapter} for fluent syntax.
	 *
	 * @param authenticationType authentication type
	 * @return API client adapter
	 */
	@SuppressWarnings("resource")
	public ApiClientFluentAdapter client(final AuthenticationType authenticationType) {
		ExchangeClient exchangeClient = getExchangeClient(authenticationType);
		return client(exchangeClient);
	}

	/**
	 * Returns an {@link ApiClientFluentAdapter} for fluent syntax.
	 *
	 * @return API client adapter
	 */
	public ApiClientFluentAdapter client() {
		return client(computeAuthenticationType());
	}

	/**
	 * Tries to compute the authentication type if none was provided.
	 *
	 * @return the authentication type if none was provided
	 */
	private AuthenticationType computeAuthenticationType() {
		Set<AuthenticationType> authenticationTypes = exchangeClientsMap.keySet();
		if (authenticationTypes.size() > 1) {
			throw new IllegalStateException("Client has multiple ExchangeClient objects please call client(AuthenticationType)");
		}
		return authenticationTypes.iterator().next();
	}

	/**
	 * Returns an exchange client builder with the given exchange client set.
	 * <p>
	 * This method is to be used in conjunction with the constructors or factory methods that accept exchange client
	 * builders. When this method is used, the API client manages the life cycle of the exchange client, so the caller
	 * should not close the exchange client.
	 *
	 * @param exchangeClientClass exchange client class
	 * @return exchange client builder
	 */
	public static ExchangeClientBuilder withClient(final Class<? extends ExchangeClient> exchangeClientClass) {
		return ExchangeClient.builder().client(exchangeClientClass);
	}

	/**
	 * Alias for {@link #withClient(Class)}.
	 * <p>
	 * This method is to be used in conjunction with the constructors or factory methods that accept exchange client
	 * builders. When this method is used, the API client manages the life cycle of the exchange client, so the caller
	 * should not close the exchange client.
	 *
	 * @param exchangeClientClass exchange client class
	 * @return exchange client builder
	 * @see #withClient(Class)
	 */
	public static ExchangeClientBuilder with(final Class<? extends ExchangeClient> exchangeClientClass) {
		return withClient(exchangeClientClass);
	}

	/**
	 * Returns an exchange client builder with the given exchange client set.
	 * <p>
	 * This method is to be used in conjunction with the constructors or factory methods that accept exchange client
	 * builders. When this method is used, the API client does not manage the life cycle of the exchange client, so the
	 * caller is responsible for closing the exchange client if needed.
	 * <p>
	 * This method should be used when an instance of exchange client is already created. Prefer using {@link #with(Class)}
	 * or {@link #withClient(Class)} when possible, so that the builder can manage the life cycle of the exchange client.
	 *
	 * @param exchangeClient exchange client
	 * @return exchange client builder
	 */
	public static ExchangeClientBuilder with(final ExchangeClient exchangeClient) {
		return ExchangeClient.builder().client(exchangeClient);
	}

	/**
	 * Returns an exchange client builder with the default exchange client and given properties set.
	 * <p>
	 * This method is to be used in conjunction with the constructors or factory methods that accept exchange client
	 * builders.
	 *
	 * @param properties client properties
	 * @return exchange client builder
	 */
	public static ExchangeClientBuilder with(final ClientProperties properties) {
		return withDefaultClient().properties(properties);
	}

	/**
	 * Returns an exchange client builder with the default exchange client.
	 * <p>
	 * This method is to be used in conjunction with the constructors or factory methods that accept exchange client
	 * builders.
	 *
	 * @return exchange client builder
	 */
	public static ExchangeClientBuilder withDefaultClient() {
		return ExchangeClient.builder().withDefaultClient();
	}

	/**
	 * API call for resource.
	 *
	 * @param <T> response type
	 *
	 * @param apiRequest API request object
	 * @return API response object
	 */
	@SuppressWarnings("resource")
	public <T> ApiResponse<T> exchange(final ApiRequest<T> apiRequest) {
		ExchangeClient exchangeClient = getExchangeClient(apiRequest.getAuthenticationType());

		BasicMeters activeMeters = getActiveMeters(apiRequest);
		Retry activeRetry = getActiveRetry(apiRequest);
		DurationAccumulator durationAccumulator = DurationAccumulator.of();

		ApiResponse<T> apiResponse = activeRetry.until(
				() -> exchange(apiRequest, exchangeClient, activeMeters),
				ApiResponse::isSuccessful,
				(response, duration) -> logExchange(apiRequest, response, duration),
				e -> activeMeters.retries().increment(),
				durationAccumulator);

		return isBleedExceptions() && apiResponse.hasException()
				? Unchecked.reThrow(apiResponse.getException())
				: apiResponse;
	}

	/**
	 * Asynchronous API call for resource.
	 * <p>
	 * TODO: implement proper async handling with non-blocking IO in the exchange clients.
	 *
	 * @param <T> response type
	 *
	 * @param apiRequest API request object
	 * @return API response object
	 */
	public <T> CompletableFuture<ApiResponse<T>> asyncExchange(final ApiRequest<T> apiRequest) {
		return CompletableFuture.supplyAsync(() -> exchange(apiRequest));
	}

	/**
	 * API call for resource with meters on the given exchange client.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest API request object
	 * @param exchangeClient the exchange client doing the request
	 * @param activeMeters the metrics for the exchange
	 * @return API response object
	 */
	private <T> ApiResponse<T> exchange(final ApiRequest<T> apiRequest, final ExchangeClient exchangeClient, final BasicMeters activeMeters) {
		return activeMeters.wrap(
				() -> exchangeClient.exchange(apiRequest),
				e -> buildErrorResponse(e, apiRequest, exchangeClient));
	}

	/**
	 * Logs the exchange.
	 *
	 * @param <T> body type
	 * @param apiRequest API request object
	 * @param apiResponse API response object
	 * @param duration the duration of the exchange
	 */
	private <T> void logExchange(final ApiRequest<T> apiRequest, final ApiResponse<T> apiResponse, final Duration duration) {
		if (apiResponse.isSuccessful()) {
			ExchangeLogger.logSuccess(LOGGER::debug, getClass(), apiRequest, apiResponse, duration);
		} else {
			ExchangeLogger.logError(LOGGER::error, getClass(), apiRequest, apiResponse, duration);
		}
	}

	/**
	 * Builds an error response.
	 *
	 * @param <T> response body type
	 *
	 * @param exception exception that represents the error
	 * @param apiRequest the API request object that caused the error
	 * @param exchangeClient the exchange client that made the failed request
	 * @return API error response object
	 */
	protected <T> ApiResponse<T> buildErrorResponse(final Exception exception, final ApiRequest<T> apiRequest, final ExchangeClient exchangeClient) {
		return ApiResponse.<T>builder()
				.request(apiRequest)
				.exception(exception)
				.errorMessagePrefix("Exchange error: ")
				.exchangeClient(exchangeClient)
				.build();
	}

	/**
	 * Returns the active meters.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest the API request object
	 * @return the active meters
	 */
	protected <T> BasicMeters getActiveMeters(final ApiRequest<T> apiRequest) {
		return isMetricsEnabled()
				? Nullables.nonNullOrDefault(apiRequest.getMeters(), this::getMeters)
				: BasicMeters.DEFAULT;
	}

	/**
	 * Returns the active retry.
	 *
	 * @param <T> request body type
	 *
	 * @param apiRequest the API request object
	 * @return the active retry
	 */
	protected <T> Retry getActiveRetry(final ApiRequest<T> apiRequest) {
		return Nullables.nonNullOrDefault(apiRequest.getRetry(), this::getRetry);
	}

	/**
	 * Returns true if the client re-throws exceptions to the caller.
	 *
	 * @return true if the client re-throws exceptions
	 */
	public boolean isBleedExceptions() {
		return bleedExceptions;
	}

	/**
	 * Set if the client should re-throw or not re-throw caught exceptions.
	 *
	 * @param bleedExceptions value to set
	 */
	public void setBleedExceptions(final boolean bleedExceptions) {
		this.bleedExceptions = bleedExceptions;
	}

	/**
	 * Returns the exchange client by authentication type. The caller should not close the returned exchange client. The
	 * {@link ApiClient} class handles the correct closing of all the associated exchange clients.
	 *
	 * @param authenticationType authentication type
	 * @return an exchange client
	 */
	public ExchangeClient getExchangeClient(final AuthenticationType authenticationType) {
		return Nullables.apply(exchangeClientsMap.get(authenticationType), ScopedResource::unwrap, () -> {
			throw new IllegalStateException("No ExchangeClient found for authentication type: " + authenticationType);
		});
	}

	/**
	 * Returns the retry object.
	 *
	 * @return the retry object
	 */
	public Retry getRetry() {
		return retry;
	}

	/**
	 * Sets the retry object for all requests.
	 *
	 * @param retry retry
	 */
	public void setRetry(final Retry retry) {
		this.retry = retry;
	}

	/**
	 * Returns the metrics enabled flag.
	 *
	 * @return the metrics enabled flag
	 */
	public boolean isMetricsEnabled() {
		return metricsEnabled;
	}

	/**
	 * Sets the metrics enabled flag.
	 *
	 * @param metricsEnabled metrics enabled flag
	 */
	public void setMetricsEnabled(final boolean metricsEnabled) {
		this.metricsEnabled = metricsEnabled;
	}

	/**
	 * Returns the basic meters object.
	 *
	 * @return the basic meters object
	 */
	public BasicMeters getMeters() {
		return meters;
	}

	/**
	 * Sets the basic meters object for all requests.
	 *
	 * @param meters meters to set
	 */
	public void setMeters(final BasicMeters meters) {
		this.meters = meters;
	}

	/**
	 * Returns the meter factory.
	 *
	 * @return the meter factory
	 */
	public MeterFactory getMeterFactory() {
		return meterFactory;
	}

	/**
	 * Sets the meter factory.
	 *
	 * @param meterFactory meter factory to set
	 */
	public void setMeterFactory(final MeterFactory meterFactory) {
		this.meterFactory = meterFactory;
	}
}
