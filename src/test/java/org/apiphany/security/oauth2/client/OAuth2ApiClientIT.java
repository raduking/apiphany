package org.apiphany.security.oauth2.client;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

import org.apiphany.ApiClient;
import org.apiphany.client.http.JavaNetHttpExchangeClient;
import org.apiphany.io.ByteBufferInputStream;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;
import org.apiphany.utils.security.ssl.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	private static final String KEYCLOAK_REALM_NAME = "test-realm";

	@SuppressWarnings("resource")
	@Container
	private static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer()
			.withRealmImportFile("security/oauth2/keycloak-realm-config.json");

	private OAuth2ClientRegistration clientRegistration;
	private OAuth2ProviderDetails providerDetails;

	private JavaNetHttpExchangeClient exchangeClient;

	@SuppressWarnings("resource")
	public static void logJWTSigningPublicKey(final String authServerUrl) throws Exception {
		String openidConfigUrl = String.format("%s/realms/%s/.well-known/openid-configuration", authServerUrl, KEYCLOAK_REALM_NAME);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode config = mapper.readTree(URI.create(openidConfigUrl).toURL());
		String jwksUri = config.get("jwks_uri").asText();

		JsonNode jwks = mapper.readTree(URI.create(jwksUri).toURL());
		JsonNode keys = jwks.get("keys");
		JsonNode key = keys.get(0);
		JsonNode x5cs = key.get("x5c");

		String x5c = x5cs.get(0).asText();
		byte[] der = Base64.getDecoder().decode(x5c);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) cf.generateCertificate(ByteBufferInputStream.of(der));
		PublicKey publicKey = cert.getPublicKey();
		String base64PublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());

		LOGGER.info("Keycloak certificate (PEM public key):\n{}", convertToPem(base64PublicKey));
	}

	public static String convertToPem(final String x5cCert) {
		StringBuilder pem = new StringBuilder();
		pem.append("-----BEGIN PUBLIC KEY-----\n");
		String base64 = x5cCert.replaceAll("(.{64})", "$1\n");
		pem.append(base64);
		pem.append("\n-----END PUBLIC KEY-----");
		return pem.toString();
	}

	@BeforeEach
	void setUp() throws Exception {
		String authServerUrl = KEYCLOAK_CONTAINER.getAuthServerUrl();
		LOGGER.info("Authentication Server URL: {}", authServerUrl);
		assertThat(authServerUrl, notNullValue());

		logJWTSigningPublicKey(authServerUrl);

		String clientRegistrationJsonString = Strings.fromFile("/security/oauth2/oauth2-client-registration.json");
		clientRegistration = JsonBuilder.fromJson(clientRegistrationJsonString, OAuth2ClientRegistration.class);

		String providerDetailsJsonString = Strings.fromFile("/security/oauth2/oauth2-provider-details.json");
		providerDetails = JsonBuilder.fromJson(providerDetailsJsonString, OAuth2ProviderDetails.class);
		providerDetails.setTokenUri(KEYCLOAK_CONTAINER.getAuthServerUrl() + KEYCLOAK_TOKEN_PATH);

		exchangeClient = new JavaNetHttpExchangeClient();
	}

	@AfterEach
	void tearDown() throws Exception {
		exchangeClient.close();
	}

	@Test
	void shouldReturnAuthenticationTokenWithClientSecretPost() throws Exception {
		try (OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient)) {
			AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_POST);

			assertThat(token, notNullValue());
		}
	}

	@Test
	void shouldReturnAuthenticationTokenWithClientSecretBasic() throws Exception {
		try (OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient)) {
			AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);

			assertThat(token, notNullValue());
		}
	}

	@Test
	void shouldReturnAuthenticationTokenWithClientAuthenticationMethodSetInClientRegistration() throws Exception {
		try (OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(clientRegistration, providerDetails, exchangeClient)) {
			AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken();

			assertThat(token, notNullValue());
		}
	}

	@Test
	void shouldReturnAuthenticationTokenWithClientSecretJwt() throws Exception {
		String clientRegistrationJsonString =
				Strings.fromFile("/security/oauth2/oauth2-client-registration-jwt.json");
		OAuth2ClientRegistration jwtClientRegistration =
				JsonBuilder.fromJson(clientRegistrationJsonString, OAuth2ClientRegistration.class);

		try (OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(jwtClientRegistration, providerDetails, exchangeClient)) {
			AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.CLIENT_SECRET_JWT);

			assertThat(token, notNullValue());
		}
	}

	@Test
	void shouldReturnAuthenticationTokenWithClientSecretPrivateKey() throws Exception {
		String clientRegistrationJsonString =
				Strings.fromFile("/security/oauth2/oauth2-client-registration-pk.json");
		OAuth2ClientRegistration pkClientRegistration =
				JsonBuilder.fromJson(clientRegistrationJsonString, OAuth2ClientRegistration.class);

		RSAPrivateKey privateKey = Keys.loadRSAPrivateKey("/security/oauth2/rsa_private.pem");

		try (OAuth2ApiClient oAuth2ApiClient = new OAuth2ApiClient(pkClientRegistration, providerDetails, privateKey, "RS256", exchangeClient)) {
			AuthenticationToken token = oAuth2ApiClient.getAuthenticationToken(ClientAuthenticationMethod.PRIVATE_KEY_JWT);

			assertThat(token, notNullValue());
		}
	}

}
