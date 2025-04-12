package org.apiphany;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apiphany.client.ExchangeClient;
import org.apiphany.lang.Strings;
import org.apiphany.lang.accumulator.ExceptionsAccumulator;
import org.apiphany.lang.retry.Retry;
import org.apiphany.meters.BasicMeters;
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
public class ApiClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

	/**
	 * <code>"api"</code> string used in most APIs.
	 */
	public static final String API = "api";

	/**
	 * No base URL provided to be used when constructing the API client object.
	 */
	public static final String NO_BASE_URL = "";

	/**
	 * The URL as string as the base path.
	 */
	private String baseUrl;

	/**
	 * Normally the client handles exceptions gracefully by logging the full exception and retransmitting the exception
	 * message to the caller.
	 * <p>
	 * Change this to <code><b>true</b></code> if you want the client to re-throw the caught exceptions (one reason for
	 * example would be to handle the exceptions differently in accordance with the needs of the application).
	 * <p>
	 * The default value is <code><b>false</b></code>.
	 */
	private boolean bleedExceptions = false;

	/**
	 * Retry for API calls. By default, no retry is configured.
	 */
	private Retry retry = Retry.NO_RETRY;

	/**
	 * Basic meters. By default all metrics are empty.
	 */
	private BasicMeters meters = BasicMeters.DEFAULT;

	/**
	 * Metrics enable/disable flag.
	 */
	private boolean metricsEnabled = true;

	/**
	 * Exchange clients map based on authentication type.
	 */
	private final Map<AuthenticationType, ExchangeClient> exchangeClientsMap = new ConcurrentHashMap<>();

	/**
	 * Constructor with exchange clients.
	 *
	 * @param baseUrl base URL to which all paths will be appended
	 * @param exchangeClients list of exchange clients
	 */
	protected ApiClient(
			final String baseUrl,
			final List<ExchangeClient> exchangeClients) {
		LOGGER.debug("Initializing: {}(baseUrl: {})", getClass().getSimpleName(), Strings.isEmpty(baseUrl) ? "<no-base-url>" : baseUrl);
		this.baseUrl = baseUrl;

		for (ExchangeClient exchangeClient : exchangeClients) {
			AuthenticationType authenticationType = exchangeClient.getAuthenticationType();
			if (this.exchangeClientsMap.containsKey(authenticationType)) {
				throw new IllegalStateException("Failed to instantiate [" + getClass() +
						"]: More than one " + ExchangeClient.class + " with type " + authenticationType + " found.");
			}
			this.exchangeClientsMap.put(authenticationType, exchangeClient);
		}

		initializeTypeObjects(this);
	}

	/**
	 * Constructor with only one exchange client.
	 *
	 * @param baseUrl base URL to which all paths will be appended
	 * @param exchangeClient exchange client
	 */
	protected ApiClient(
			final String baseUrl,
			final ExchangeClient exchangeClient) {
		this(baseUrl, Collections.singletonList(exchangeClient));
	}

	/**
	 * Constructor with only one exchange client and no base url.
	 *
	 * @param exchangeClient exchange client
	 */
	protected ApiClient(
			final ExchangeClient exchangeClient) {
		this(NO_BASE_URL, Collections.singletonList(exchangeClient));
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
				Field typeField = Fields.getDeclaredFieldInHierarchy(GenericClass.class, "type");
				Fields.IgnoreAccess.set(typeObject, typeField, actualTypeArgument);
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
	public ApiClientFluentAdapter client(final ExchangeClient exchangeClient) {
		return ApiClientFluentAdapter.of(this)
				.authenticationType(exchangeClient.getAuthenticationType());
	}

	/**
	 * Returns an {@link ApiClientFluentAdapter} for fluent syntax.
	 *
	 * @param authenticationType authentication type
	 * @return API client adapter
	 */
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
	 * API call for resource.
	 *
	 * @param <T> response type
	 *
	 * @param apiRequest API request object
	 * @return API response object
	 */
	public <T> ApiResponse<T> exchange(final ApiRequest<T> apiRequest) {
		BasicMeters activeMeters = getActiveMeters(apiRequest);
		Retry activeRetry = Nullables.nonNullOrDefault(apiRequest.getRetry(), this::getRetry);

		ExceptionsAccumulator accumulator = ExceptionsAccumulator.of();
		Duration duration = Duration.ZERO;
		ApiResponse<T> apiResponse;
		activeMeters.requests().increment();
		ExchangeClient exchangeClient = getExchangeClient(apiRequest.getAuthenticationType());
		Instant start = Instant.now();
		try {
			apiResponse = activeRetry.when(
					() -> exchangeClient.exchange(apiRequest), ApiPredicates.nonNullResponse(),
					e -> activeMeters.retries().increment(), accumulator);
			duration = Duration.between(start, Instant.now());
			RequestLogger.logSuccess(LOGGER::debug, this, apiRequest, apiResponse, duration);
		} catch (Exception exception) {
			duration = Duration.between(start, Instant.now());
			activeMeters.errors().increment();
			RequestLogger.logError(LOGGER::debug, this, apiRequest, duration, exception);
			LOGGER.error("EXCEPTION: ", exception);
			apiResponse = errorResponse(exception, exchangeClient);
		} finally {
			activeMeters.latency().record(duration);
		}
		if (isBleedExceptions() && apiResponse.hasException()) {
			Unchecked.reThrow(apiResponse.getException());
		}
		return apiResponse;
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
	protected <T> ApiResponse<T> errorResponse(final Exception exception, final ExchangeClient exchangeClient) {
		return ApiResponse.<T>builder()
				.exception(exception)
				.errorMessagePrefix("API error: ")
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
	 * Asynchronous API call for resource.
	 *
	 * @param <T> response type
	 *
	 * @param apiRequest API request object
	 * @return API response object
	 */
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
	 * Returns the exchange client by authentication type.
	 *
	 * @param authenticationType authentication type
	 * @return an exchange client
	 */
	protected ExchangeClient getExchangeClient(final AuthenticationType authenticationType) {
		return Nullables.nonNullOrDefault(exchangeClientsMap.get(authenticationType), () -> {
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
}
