package org.apiphany.client;

import java.util.Objects;

import org.apiphany.ApiRequest;
import org.apiphany.lang.ScopedResource;

/**
 * Base class for HTTP exchange clients that decorate another exchange client.
 * <p>
 * The implementation is just a pass-through to the underlying exchange client (a simple delegate) but subclasses may
 * override {@link #exchange(ApiRequest)} to add custom behavior (e.g., request/response modification, authentication,
 * logging, metrics, etc.).
 *
 * @author Radu Sebastian LAZIN
 */
public class DecoratingExchangeClient implements DelegatingExchangeClient {

	/**
	 * The actual exchange client doing the request.
	 */
	protected final ScopedResource<ExchangeClient> exchangeClient;

	/**
	 * Initialize the client with the given exchange client delegate scoped resource.
	 *
	 * @param delegate actual exchange client making the request
	 * @throws NullPointerException if delegate is null
	 */
	public DecoratingExchangeClient(final ScopedResource<ExchangeClient> delegate) {
		this.exchangeClient = Objects.requireNonNull(delegate, "delegate cannot be null");
	}

	/**
	 * Initialize the client with the given exchange client delegate. The delegate is considered unmanaged so that the
	 * caller must take care of closing it. Only use this constructor when the life cycle of the delegate is managed outside
	 * of this class.
	 *
	 * @param delegate actual exchange client making the request
	 */
	public DecoratingExchangeClient(final ExchangeClient delegate) {
		this(ScopedResource.unmanaged(delegate));
	}

	/**
	 * Returns the delegate scoped resource exchange client.
	 *
	 * @return the delegate scoped resource exchange client
	 */
	public ScopedResource<ExchangeClient> getDelegate() {
		return exchangeClient;
	}

	/**
	 * Returns the delegate exchange client.
	 *
	 * @return the delegate exchange client
	 */
	@Override
	public ExchangeClient getExchangeClient() {
		return getDelegate().unwrap();
	}

	/**
	 * Closes the underlying exchange client if it is managed.
	 * <p>
	 * Implementations overriding this method should call {@code super.close()} to ensure proper resource cleanup.
	 *
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close() throws Exception {
		exchangeClient.closeIfManaged();
	}
}
