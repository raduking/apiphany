package org.apiphany.tests.contract;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apiphany.ApiClient;
import org.apiphany.Status;
import org.apiphany.client.ClientProperties;
import org.apiphany.http.HttpException;
import org.apiphany.security.ssl.KeyStoreType;
import org.apiphany.security.ssl.SSLProperties;
import org.apiphany.security.ssl.StoreInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public interface TLSContract extends ApiphanyContract {

	@DisplayName("TLS Basic: should throw exception if client SSL context is not configured")
	@Test
	default void shouldThrowExceptionIfClientSSLContextIsNotConfigured() throws Exception {
		wiremock().stubFor(get("/hello")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("world")));

		ApiClient api = apiClient();
		try (api) {
			HttpException exception = assertThrows(HttpException.class, () -> api.client() // NOSONAR
					.http()
					.get()
					.path("hello")
					.retrieve(String.class)
					.orRethrow());

			assertEquals(Status.UNKNOWN, exception.getStatusCode());
		}

		wiremock().verify(0, getRequestedFor(urlEqualTo("/hello")));
	}

	@DisplayName("TLS Basic: The client should be able to make a simple GET request and return the response body when the client SSL context is properly configured")
	@Test
	default void shouldReturnBody() throws Exception {
		wiremock().stubFor(get("/hello")
				.willReturn(aResponse()
						.withStatus(200)
						.withBody("world")));

		ClientProperties properties = clientProperties();
		SSLProperties sslProperties = new SSLProperties();

		StoreInfo clientKeyStore = new StoreInfo();
		clientKeyStore.setLocation("security/ssl/client-keystore.jks");
		clientKeyStore.setPassword("clientkeystorepass123".toCharArray());
		clientKeyStore.setType(KeyStoreType.JKS.value());
		clientKeyStore.setExternal(false);

		StoreInfo clientTrustStore = new StoreInfo();
		clientTrustStore.setLocation("security/ssl/client-truststore.jks");
		clientTrustStore.setPassword("clienttruststorepass123".toCharArray());
		clientTrustStore.setType(KeyStoreType.JKS.value());
		clientTrustStore.setExternal(false);

		sslProperties.setKeystore(clientKeyStore);
		sslProperties.setTruststore(clientTrustStore);

		properties.setCustomProperties(sslProperties);

		ApiClient api = apiClient(properties);
		try (api) {
			String result = api.client()
					.http()
					.get()
					.path("hello")
					.retrieve(String.class)
					.orNull();

			assertEquals("world", result);
		}

		wiremock().verify(1, getRequestedFor(urlEqualTo("/hello")));
	}
}
