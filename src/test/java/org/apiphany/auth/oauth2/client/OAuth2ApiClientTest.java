package org.apiphany.auth.oauth2.client;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apiphany.auth.AuthenticationToken;
import org.apiphany.auth.oauth2.OAuth2ProviderDetails;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.morphix.lang.thread.Threads;

/**
 * Test class for {@link OAuth2ApiClient}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ApiClientTest {

	private static final NimbusOAuth2Server OAUTH2_SERVER = new NimbusOAuth2Server();

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	@BeforeEach
	void setUp() {
		clientRegistration =
				JsonBuilder.fromJson(Strings.fromFile("/oauth2-client-registration.json", Threads.noConsumer()), OAuth2ClientRegistration.class);
		providerDetails = JsonBuilder.fromJson(Strings.fromFile("/oauth2-provider-details.json", Threads.noConsumer()), OAuth2ProviderDetails.class);
	}

	@Test
	void shouldReturnAuthenticationTokenWithNimbusOAuth2Server() {
		providerDetails.setTokenUri(OAUTH2_SERVER.getUrl() + "/token");

		OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, new HttpExchangeClient());

		AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

		assertThat(token, notNullValue());
	}

}
