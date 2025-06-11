package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.ApiClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dasniko.testcontainers.keycloak.KeycloakContainer;

/**
 * Integration Test class for {@link OAuth2ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
class OAuth2ApiClientIT {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiClient.class);

	private static final String KEYCLOAK_TOKEN_PATH = "/realms/test-realm/protocol/openid-connect/token";

	@SuppressWarnings("resource")
	@Container
	private static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer()
			.withRealmImportFile("keycloak-realm-config.json");

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	@BeforeEach
	void setUp() {
		String authServerUrl = KEYCLOAK_CONTAINER.getAuthServerUrl();
		LOGGER.info("Authentication Server URL: {}", authServerUrl);
		assertThat(authServerUrl, notNullValue());

		clientRegistration = JsonBuilder.fromJson(Strings.fromFile("/oauth2-client-registration.json"), OAuth2ClientRegistration.class);

		providerDetails = JsonBuilder.fromJson(Strings.fromFile("/oauth2-provider-details.json"), OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(KEYCLOAK_CONTAINER.getAuthServerUrl() + KEYCLOAK_TOKEN_PATH);
	}

	@Test
	void shouldReturnAuthenticationTokenWithClientSecretPost() {
		OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, new JavaNetHttpExchangeClient());

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		assertThat(token, notNullValue());
	}

	@Test
	void shouldReturnAuthenticationTokenWithClientSecretBasic() {
		OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, new JavaNetHttpExchangeClient());

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);

		assertThat(token, notNullValue());
	}

	@Test
	void shouldReturnAuthenticationTokenWithClientAuthenticationMethodSetInClientRegistration() {
		OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, new JavaNetHttpExchangeClient());

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken();

		assertThat(token, notNullValue());
	}
}
