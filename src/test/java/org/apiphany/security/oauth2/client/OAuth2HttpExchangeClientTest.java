package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.Map;

import org.apiphany.ApiClient;
import org.apiphany.client.ClientProperties;
import org.apiphany.client.http.HttpExchangeClient;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.net.Sockets;
import org.apiphany.security.oauth2.OAuth2Properties;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.security.oauth2.server.JwtTokenValidator;
import org.apiphany.security.oauth2.server.SimpleHttpApiServer;
import org.apiphany.security.oauth2.server.SimpleOAuth2Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class OAuth2HttpExchangeClientTest {

	private static final String CLIENT_SECRET = "apiphany-client-secret-more-than-32-characters";
	private static final String CLIENT_ID = "apiphany-client";
	private static final String PROVIDER = "provider";
	private static final String PROVIDER_NAME = "my-provider-name";
	private static final String REGISTRATION = "registration";
	private static final String MY_SIMPLE_APP = "my-simple-app";

	private static final Duration PORT_CHECK_TIMEOUT = Duration.ofMillis(500);
	private static final int OAUTH_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);
	private static final int API_SERVER_PORT = Sockets.findAvailableTcpPort(PORT_CHECK_TIMEOUT);

	private static final SimpleOAuth2Server OAUTH2_SERVER = new SimpleOAuth2Server(OAUTH_SERVER_PORT, CLIENT_ID, CLIENT_SECRET);
	private static final JwtTokenValidator JWT_TOKEN_VALIDATOR = new JwtTokenValidator(CLIENT_ID, CLIENT_SECRET, OAUTH2_SERVER.getUrl());

	@SuppressWarnings("unused")
	private static final SimpleHttpApiServer API_SERVER = new SimpleHttpApiServer(API_SERVER_PORT, JWT_TOKEN_VALIDATOR);

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private SimpleApiClient simpleApiClient;

	private ClientProperties clientProperties;

	@BeforeEach
	void setUp() {
		clientRegistration = JsonBuilder.fromJson(Strings.fromFile("/oauth2-client-registration.json"), OAuth2ClientRegistration.class);
		clientRegistration.setProvider(PROVIDER_NAME);
		clientRegistration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST);
		providerDetails = JsonBuilder.fromJson(Strings.fromFile("/oauth2-provider-details.json"), OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(OAUTH2_SERVER.getUrl() + "/token");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> clientRegistrationMap = mapper.convertValue(clientRegistration, new TypeReference<Map<String, Object>>() {
			// empty
		});
		Map<String, Object> providerDetailsMap = mapper.convertValue(providerDetails, new TypeReference<Map<String, Object>>() {
			// empty
		});
		var oAuth2Properties = Map.<String, Object>of(
				REGISTRATION, Map.of(MY_SIMPLE_APP, clientRegistrationMap),
				PROVIDER, Map.of(PROVIDER_NAME, providerDetailsMap)
		);
		var custom = Map.<String, Object>of(OAuth2Properties.ROOT, oAuth2Properties);

		clientProperties = new ClientProperties();
		clientProperties.setCustom(custom);

		simpleApiClient = new SimpleApiClient(clientProperties);
	}

	@Test
	void shouldReturnValidAuthenticationTokenWithSimpleOAuth2Server() {
		String result = simpleApiClient.getName();

		assertThat(result, equalTo(SimpleHttpApiServer.NAME));
	}

	static class SimpleApiClient extends ApiClient {

		protected SimpleApiClient(final ClientProperties properties) {
			super(new OAuth2HttpExchangeClient(new HttpExchangeClient(properties)));
		}

		public String getName() {
			return client()
					.get()
					.url("http://localhost:" + API_SERVER_PORT)
					.path(API, "name")
					.retrieve(String.class)
					.orNull();
		}
	}
}
