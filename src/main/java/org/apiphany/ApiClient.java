package org.apiphany;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.lang.Pair;
import org.apiphany.lang.Strings;
import org.apiphany.lang.accumulator.DurationAccumulator;
import org.apiphany.lang.retry.Retry;
import org.apiphany.meters.BasicMeters;
import org.apiphany.security.AuthenticationType;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.Nullables;
import org.morphix.lang.Unchecked;
import org.morphix.lang.function.ThrowingBiConsumer;
import org.morphix.reflection.Fields;
import org.morphix.reflection.GenericClass;
import org.morphix.reflection.predicates.MemberPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.MeterRegistry;

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
	 * The meter registry.
	 */
	private MeterRegistry meterRegistry;

	/**
	 * Exchange clients map based on authentication type and a pair of exchange client and managed flag that tells the
	 * API client if the exchange client's life cycle is managed by the API client or not.
	 */
	private final Map<AuthenticationType, Pair<ExchangeClient, Boolean>> exchangeClientsMap = new ConcurrentHashMap<>();

	/**
	 * Constructor with exchange clients.
	 *
	 * @param baseUrl base URL to which all paths will be appended
	 * @param exchangeClients list of exchange clients
	 * @param managed flag that indicates whether the exchange clients are managed
	 */
	@SuppressWarnings("resource")
	protected ApiClient(final String baseUrl, final List<ExchangeClient> exchangeClients, final boolean managed) {
		LOGGER.debug("Initializing: {}(baseUrl: {})", getClass().getSimpleName(), Strings.isEmpty(baseUrl) ? "<no-base-url>" : baseUrl);
		this.baseUrl = baseUrl;

		for (ExchangeClient exchangeClient : exchangeClients) {
			AuthenticationType authenticationType = exchangeClient.getAuthenticationType();
			this.exchangeClientsMap.merge(authenticationType, Pair.of(exchangeClient, managed), (oldValue, newValue) -> {
				throw new IllegalStateException("Failed to instantiate [" + getClass()
						+ "]: For authentication type " + authenticationType + ", " + oldValue.left().getName() + " already exists");
			});
		}
		initializeTypeObjects(this);
	}

	/**
	 * Constructor with exchange clients.
	 *
	 * @param baseUrl base URL to which all paths will be appended
	 * @param exchangeClients list of exchange clients
	 */
	protected ApiClient(final String baseUrl, final List<ExchangeClient> exchangeClients) {
		this(baseUrl, exchangeClients, false);
	}

	/**
	 * Constructor with only one exchange client.
	 *
	 * @param baseUrl base URL to which all paths will be appended
	 * @param exchangeClient exchange client
	 */
	protected ApiClient(final String baseUrl, final ExchangeClient exchangeClient) {
		this(baseUrl, Collections.singletonList(exchangeClient));
	}

	/**
	 * Constructor with only one exchange client and no base URL.
	 *
	 * @param exchangeClient exchange client
	 */
	protected ApiClient(final ExchangeClient exchangeClient) {
		this(EMPTY_BASE_URL, Collections.singletonList(exchangeClient));
	}

	/**
	 * Constructor with base URL and exchange client builder, the built exchange client will be managed by this client.
	 *
	 * @param baseUrl base URL
	 * @param exchangeClientBuilder exchange client builder
	 */
	@SuppressWarnings("resource")
	protected ApiClient(final String baseUrl, final ExchangeClientBuilder exchangeClientBuilder) {
		this(baseUrl, Collections.singletonList(exchangeClientBuilder.build()), true);
	}

	/**
	 * Constructor with exchange client builder, the built exchange client will be managed by this client.
	 *
	 * @param exchangeClientBuilder exchange client builder
	 */
	protected ApiClient(final ExchangeClientBuilder exchangeClientBuilder) {
		this(EMPTY_BASE_URL, exchangeClientBuilder);
	}

	/**
	 * @see #close()
	 */
	@Override
	public void close() throws Exception {
		exchangeClientsMap.forEach(ThrowingBiConsumer.unchecked((k, v) -> {
			if (v.right().booleanValue()) {
				v.left().close();
			}
		}));
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
	 * @param exchangeClients exchange client objects
	 * @return a new ApiClient object
	 */
	public static ApiClient of(final String baseUrl, final List<ExchangeClient> exchangeClients) {
		return new ApiClient(baseUrl, exchangeClients);
	}

	/**
	 * Returns a new {@link ApiClient} object.
	 *
	 * @param baseUrl base URL
	 * @param exchangeClientBuilder exchange client object builder
	 * @return a new ApiClient object
	 */
	public static ApiClient of(final String baseUrl, final ExchangeClientBuilder exchangeClientBuilder) {
		return new ApiClient(baseUrl, exchangeClientBuilder);
	}

	/**
	 * Initializes the defined type objects in derived classes and initialized with {@link #typeObject()}.
	 *
	 * @param apiClient API Client
	 */
	private static void initializeTypeObjects(final ApiClient apiClient) {
		Predicate<Field> predicate = MemberPredicates.withAllModifiers(Modifier::isStatic, Modifier::isFinal);
		Fields.getDeclaredFieldsInHierarchy(apiClient.getClass(), predicate)
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
		if (typeObject.getType() instanceof TypeVariable) {
			ParameterizedType parameterizedType = JavaObjects.cast(typeObjectField.getGenericType());
			Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
			if (actualTypeArgument instanceof ParameterizedType) {
				typeObject.setType(actualTypeArgument);
			} else {
				throw new IllegalArgumentException("typeObject should only be used for generic types, current type: "
						+ actualTypeArgument.getTypeName() + " is not a generic type.");
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
		return new GenericClass<>() {
			// empty
		};
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
		if (authenticationTypes.isEmpty()) {
			throw new IllegalStateException("No ExchangeClient has been set before calling client()");
		}
		if (authenticationTypes.size() > 1) {
			throw new IllegalStateException("Client has multiple ExchangeClient objects please call client(AuthenticationType)");
		}
		return authenticationTypes.iterator().next();
	}

	/**
	 * Returns an exchange client builder with the given exchange client set.
	 *
	 * @param exchangeClient exchange client
	 * @return exchange client builder
	 */
	public static ExchangeClientBuilder exchangeClient(final Class<? extends ExchangeClient> exchangeClient) {
		return ExchangeClient.builder().client(exchangeClient);
	}

	/**
	 * Alias for {@link #exchangeClient(Class)}.
	 *
	 * @param exchangeClient exchange client
	 * @return exchange client builder
	 */
	public static ExchangeClientBuilder with(final Class<? extends ExchangeClient> exchangeClient) {
		return exchangeClient(exchangeClient);
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
		DurationAccumulator durationAccumulator = DurationAccumulator.of();

		ApiResponse<T> apiResponse = getActiveRetry(apiRequest).when(
				() -> exchange(apiRequest, exchangeClient, activeMeters),
				(response, duration) -> logExchange(apiRequest, response, duration),
				ApiResponse::isSuccessful,
				e -> activeMeters.retries().increment(),
				durationAccumulator);

		if (isBleedExceptions() && apiResponse.hasException()) {
			Unchecked.reThrow(apiResponse.getException());
		}
		return apiResponse;
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
				e -> getErrorResponse(e, exchangeClient));
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
	 * @param exchangeClient the exchange client that made the failed request
	 * @return API error response object
	 */
	protected <T> ApiResponse<T> getErrorResponse(final Exception exception, final ExchangeClient exchangeClient) {
		return ApiResponse.<T>builder()
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
	 * Asynchronous API call for resource.
	 *
	 * @param <T> response type
	 *
	 * @param apiRequest API request object
	 * @return API response object
	 */
	@SuppressWarnings("resource")
	public <T> CompletableFuture<ApiResponse<T>> asyncExchange(final ApiRequest<T> apiRequest) {
		ExchangeClient exchangeClient = getExchangeClient(apiRequest.getAuthenticationType());
		return exchangeClient.asyncExchange(apiRequest);
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
	 * Returns the exchange client by authentication type. The returned exchange client should not be closed by the caller.
	 * The {@link ApiClient} class handles the correct closing of all the associated exchange clients.
	 *
	 * @param authenticationType authentication type
	 * @return an exchange client
	 */
	protected ExchangeClient getExchangeClient(final AuthenticationType authenticationType) {
		return Nullables.apply(exchangeClientsMap.get(authenticationType), Pair::left, () -> {
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
	 * Returns the meter registry.
	 *
	 * @return the meter registry
	 */
	public MeterRegistry getMeterRegistry() {
		return meterRegistry;
	}

	/**
	 * Sets the meter registry.
	 *
	 * @param meterRegistry meter registry to set
	 */
	public void setMeterRegistry(final MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}
}
