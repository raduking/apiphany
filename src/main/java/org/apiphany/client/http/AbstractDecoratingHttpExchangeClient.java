package org.apiphany.client.http;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.ExchangeClient;
import org.apiphany.lang.ScopedResource;

/**
 * Base class for HTTP exchange clients that decorate another exchange client.
 *
 * @author Radu Sebastian LAZIN
 */
public abstract class AbstractDecoratingHttpExchangeClient extends AbstractHttpExchangeClient {

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
	protected AbstractDecoratingHttpExchangeClient(final ScopedResource<ExchangeClient> delegate) {
		super(delegate.unwrap().getClientProperties());
		this.exchangeClient = delegate;
	}

	/**
	 * Initialize the client with the given exchange client delegate.
	 *
	 * @param delegate actual exchange client making the request
	 */
	protected AbstractDecoratingHttpExchangeClient(final ExchangeClient delegate) {
		this(ScopedResource.unmanaged(delegate));
	}

	/**
	 * Subclasses must implement this method to perform the exchange, possibly decorating the request/response as needed.
	 * The default implementation simply delegates to the underlying exchange client.
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
