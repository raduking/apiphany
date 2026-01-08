package org.apiphany.client.http;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;
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
public class DecoratingHttpExchangeClient extends AbstractHttpExchangeClient {

	/**
	 * The actual exchange client doing the request.
	 */
	protected final ScopedResource<ExchangeClient> exchangeClient;

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param delegate actual exchange client making the request
	 */
	@SuppressWarnings("resource")
	protected DecoratingHttpExchangeClient(final ScopedResource<ExchangeClient> delegate) {
		super(delegate.unwrap().getClientProperties());
		this.exchangeClient = delegate;
	}

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param delegate actual exchange client making the request
	 */
	protected DecoratingHttpExchangeClient(final ExchangeClient delegate) {
		this(ScopedResource.unmanaged(delegate));
	}

	/**
	 * Delegates the exchange to the underlying exchange client.
	 * <p>
	 * Subclasses may override this method to perform the exchange, possibly decorating the request/response as needed but
	 * they should call {@code super.exchange(...)} to delegate to the underlying exchange client.
	 *
	 * @see ExchangeClient#exchange(ApiRequest)
	 */
	@SuppressWarnings("resource")
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		return getExchangeClient().exchange(apiRequest);
	}

	/**
	 * @see #close()
	 */
	@Override
	public void close() throws Exception {
		exchangeClient.closeIfManaged();
	}

	/**
	 * Returns the delegate exchange client.
	 *
	 * @return the delegate exchange client
	 */
	protected ExchangeClient getExchangeClient() {
		return exchangeClient.unwrap();
	}
}
