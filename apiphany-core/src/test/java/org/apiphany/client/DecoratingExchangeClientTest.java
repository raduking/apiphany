package org.apiphany.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apiphany.ApiRequest;
import org.apiphany.lang.ScopedResource;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link DecoratingExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class DecoratingExchangeClientTest {

	@SuppressWarnings("resource")
	@Test
	void shouldConstructWithUnmanagedExchangeClientInstance() throws Exception {
		ExchangeClient delegate = mock(ExchangeClient.class);

		try (DecoratingExchangeClient client = new DecoratingExchangeClient(delegate)) {
			assertThat(client.getExchangeClient(), equalTo(delegate));
		} finally {
			verify(delegate, times(0)).close();
			delegate.close();
		}
	}

	@SuppressWarnings("resource")
	@Test
	void shouldConstructWithManagedExchangeClientInstance() throws Exception {
		ExchangeClient delegate = mock(ExchangeClient.class);
		ScopedResource<ExchangeClient> scopedDelegate = ScopedResource.managed(delegate);

		try (DecoratingExchangeClient client = new DecoratingExchangeClient(scopedDelegate)) {
			assertThat(client.getExchangeClient(), equalTo(delegate));
		}
		verify(delegate).close();
	}

	@SuppressWarnings("resource")
	@Test
	void shouldDelegateExchangeCall() throws Exception {
		ExchangeClient delegate = mock(ExchangeClient.class);
		ApiRequest<String> request = new ApiRequest<>();
		try (DecoratingExchangeClient client = new DecoratingExchangeClient(delegate)) {
			client.exchange(request);

			verify(delegate).exchange(request);
		} finally {
			delegate.close();
		}
	}
}
