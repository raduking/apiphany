package org.apiphany.client.http;

import java.util.concurrent.atomic.AtomicLong;

import org.apiphany.ApiRequest;
import org.apiphany.ApiResponse;
import org.apiphany.client.DecoratingExchangeClient;
import org.apiphany.client.ExchangeClient;
import org.morphix.lang.resource.ScopedResource;

/**
 * Exchange client decorator that counts the number of requests made through the client.
 *
 * @author Radu Sebastian LAZIN
 */
public class CountingHttpExchangeClient extends DecoratingExchangeClient implements HttpExchangeClient {

	/**
	 * The count of requests made through the client.
	 */
	private final AtomicLong requestCount = new AtomicLong(0);

	/**
	 * Constructor with the delegate exchange client.
	 *
	 * @param delegate the exchange client to decorate
	 */
	public CountingHttpExchangeClient(final ScopedResource<ExchangeClient> delegate) {
		super(delegate);
	}

	/**
	 * @see DecoratingExchangeClient#exchange(ApiRequest)
	 */
	@Override
	public <T, U> ApiResponse<U> exchange(final ApiRequest<T> apiRequest) {
		requestCount.incrementAndGet();
		return super.exchange(apiRequest);
	}

	/**
	 * Returns the count of requests made through the client.
	 *
	 * @return the request count
	 */
	public long getRequestCount() {
		return requestCount.get();
	}
}
