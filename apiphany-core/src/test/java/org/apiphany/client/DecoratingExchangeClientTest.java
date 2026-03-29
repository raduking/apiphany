package org.apiphany.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.apiphany.ApiRequest;
import org.junit.jupiter.api.Test;
import org.morphix.lang.resource.ScopedResource;

/**
 * Test class for {@link DecoratingExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class DecoratingExchangeClientTest {

	@Test
	@SuppressWarnings("resource")
	void shouldConstructWithUnmanagedExchangeClientInstance() throws Exception {
		ExchangeClient delegate = mock(ExchangeClient.class);

		try (DecoratingExchangeClient client = new DecoratingExchangeClient(delegate)) {
			assertThat(client.getExchangeClient(), equalTo(delegate));
		} finally {
			verify(delegate, never()).close();
			delegate.close();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldConstructWithManagedExchangeClientInstance() throws Exception {
		ExchangeClient delegate = mock(ExchangeClient.class);
		ScopedResource<ExchangeClient> scopedDelegate = ScopedResource.managed(delegate);

		try (DecoratingExchangeClient client = new DecoratingExchangeClient(scopedDelegate)) {
			assertThat(client.getExchangeClient(), equalTo(delegate));
		}
		verify(delegate).close();
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDelegateExchangeCall() throws Exception {
		try (ExchangeClient delegate = mock(ExchangeClient.class); DecoratingExchangeClient client = new DecoratingExchangeClient(delegate)) {
			ApiRequest<String> request = new ApiRequest<>();
			client.exchange(request);

			verify(delegate).exchange(request);
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDelegateGetClientPropertiesCall() throws Exception {
		try (ExchangeClient delegate = mock(ExchangeClient.class); DecoratingExchangeClient client = new DecoratingExchangeClient(delegate)) {
			client.getClientProperties();

			verify(delegate).getClientProperties();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDelegateGetAuthenticationTypeCall() throws Exception {
		try (ExchangeClient delegate = mock(ExchangeClient.class); DecoratingExchangeClient client = new DecoratingExchangeClient(delegate)) {
			client.getAuthenticationType();

			verify(delegate).getAuthenticationType();
		}
	}
}
