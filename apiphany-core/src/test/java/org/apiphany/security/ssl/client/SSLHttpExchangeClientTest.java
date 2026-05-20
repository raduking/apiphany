package org.apiphany.security.ssl.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

import javax.net.ssl.SSLContext;

import org.apiphany.client.ClientProperties;
import org.apiphany.client.ExchangeClient;
import org.apiphany.security.AuthenticationType;
import org.apiphany.security.ssl.KeyStoreType;
import org.apiphany.security.ssl.SSLProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test class for {@link SSLHttpExchangeClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@ExtendWith(MockitoExtension.class)
class SSLHttpExchangeClientTest {

	private static final String KEYSTORE_PATH = "security/ssl/keystore.jks";
	private static final String KEYSTORE_PASSWORD = "keystorepassword123";
	private static final String TRUSTSTORE_PATH = "security/ssl/truststore.jks";
	private static final String TRUSTSTORE_PASSWORD = "truststorepassword123";

	@Mock
	private ExchangeClient exchangeClient;

	@Test
	void shouldReturnSslAuthenticationType() throws Exception {
		SSLProperties sslProperties = new SSLProperties();
		sslProperties.getKeystore().setLocation(KEYSTORE_PATH);
		sslProperties.getKeystore().setPassword(KEYSTORE_PASSWORD.toCharArray());
		sslProperties.getKeystore().setType(KeyStoreType.JKS.value());

		try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
			assertThat(client.getAuthenticationType(), equalTo(AuthenticationType.SSL));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnSslPropertiesFromClientProperties() throws Exception {
		ClientProperties clientProperties = new ClientProperties();
		SSLProperties sslProperties = new SSLProperties();
		clientProperties.setCustomProperties(sslProperties);
		doReturn(clientProperties).when(exchangeClient).getClientProperties();

		try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
			SSLProperties result = client.getSslProperties();

			assertThat(result, notNullValue());
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnSslPropertiesFromClientPropertiesWithKeystore() throws Exception {
		ClientProperties clientProperties = new ClientProperties();
		SSLProperties sslProperties = new SSLProperties();
		sslProperties.getKeystore().setLocation(KEYSTORE_PATH);
		sslProperties.getKeystore().setPassword(KEYSTORE_PASSWORD.toCharArray());
		sslProperties.getKeystore().setType("JKS");
		sslProperties.getTruststore().setLocation(TRUSTSTORE_PATH);
		sslProperties.getTruststore().setPassword(TRUSTSTORE_PASSWORD.toCharArray());
		sslProperties.getTruststore().setType("JKS");
		clientProperties.setCustomProperties(sslProperties);

		doReturn(clientProperties).when(exchangeClient).getClientProperties();

		try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
			SSLProperties result = client.getSslProperties();

			assertThat(result.getKeystore().getLocation(), equalTo(KEYSTORE_PATH));
			assertThat(result.getTruststore().getLocation(), equalTo(TRUSTSTORE_PATH));
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldDelegateClientProperties() throws Exception {
		ClientProperties clientProperties = new ClientProperties();
		doReturn(clientProperties).when(exchangeClient).getClientProperties();

		try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
			ClientProperties result = client.getClientProperties();

			assertThat(result, sameInstance(clientProperties));
		}
	}

	@Test
	void shouldReturnNullSslContextWhenDelegateNotSslContextAware() throws Exception {
		try (SSLHttpExchangeClient client = new SSLHttpExchangeClient(exchangeClient)) {
			SSLContext result = client.getSslContext();

			assertThat(result, equalTo(null));
		}
	}
}
