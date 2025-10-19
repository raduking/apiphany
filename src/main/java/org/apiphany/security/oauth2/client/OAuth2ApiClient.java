package org.apiphany.security.oauth2.client;

import static org.apiphany.ParameterFunction.parameter;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.apiphany.ApiClient;
import org.apiphany.RequestParameters;
import org.apiphany.client.ExchangeClient;
import org.apiphany.client.ExchangeClientBuilder;
import org.apiphany.http.HttpAuthScheme;
import org.apiphany.http.HttpHeader;
import org.apiphany.io.ContentType;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Strings;
import org.apiphany.security.AuthenticationException;
import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
import org.apiphany.security.JwsAlgorithm;
import org.apiphany.security.MessageDigestAlgorithm;
import org.apiphany.security.Signer;
import org.apiphany.security.oauth2.AuthorizationGrantType;
import org.apiphany.security.oauth2.ClientAuthenticationMethod;
import org.apiphany.security.oauth2.OAuth2ClientRegistration;
import org.apiphany.security.oauth2.OAuth2Parameter;
import org.apiphany.security.oauth2.OAuth2ProviderDetails;

/**
 * Specialized {@link ApiClient} for OAuth2 authentication flows. Handles token acquisition and management for OAuth2
 * protected APIs.
 * <p>
 * TODO: implement full functionality.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ApiClient extends ApiClient implements AuthenticationTokenProvider {

	/**
	 * The base64 no padding URL encoder.
	 */
	private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

	/**
	 * Configuration for the OAuth2 client registration.
	 */
	private final OAuth2ClientRegistration clientRegistration;

	/**
	 * Configuration details for the OAuth2 provider.
	 */
	private final OAuth2ProviderDetails providerDetails;

	/**
	 * The private key used when {@link ClientAuthenticationMethod#PRIVATE_KEY_JWT} is used.
	 */
	private PrivateKey privateKey;

	/**
	 * The JWS signing algorithm used when {@link ClientAuthenticationMethod#PRIVATE_KEY_JWT} is used.
	 */
	private JwsAlgorithm signingAlgorithm;

	/**
	 * Constructs a new OAuth2 API client with the specified configurations.
	 *
	 * @param clientRegistration the OAuth2 client registration details
	 * @param providerDetails the OAuth2 provider configuration details
	 * @param httpExchangeClient the HTTP exchange client to use for requests
	 */
	public OAuth2ApiClient(final OAuth2ClientRegistration clientRegistration, final OAuth2ProviderDetails providerDetails,
			final ExchangeClient httpExchangeClient) {
		super(httpExchangeClient);
		this.clientRegistration = clientRegistration;
		this.providerDetails = providerDetails;
	}

	/**
	 * Constructs a new OAuth2 API client with the specified configurations.
	 *
	 * @param clientRegistration the OAuth2 client registration details
	 * @param providerDetails the OAuth2 provider configuration details
	 * @param exchangeClientBuilder the HTTP exchange client to use for requests
	 */
	public OAuth2ApiClient(final OAuth2ClientRegistration clientRegistration, final OAuth2ProviderDetails providerDetails,
			final ExchangeClientBuilder exchangeClientBuilder) {
		super(exchangeClientBuilder);
		this.clientRegistration = clientRegistration;
		this.providerDetails = providerDetails;
	}

	/**
	 * Constructs a new OAuth2 API client with the specified configurations.
	 *
	 * @param clientRegistration the OAuth2 client registration details
	 * @param privateKey the private key used when authorizing with {@code private_key_jwt}
	 * @param signingAlgorithm the signing algorithm used to sign the client assertion
	 * @param providerDetails the OAuth2 provider configuration details
	 * @param httpExchangeClient the HTTP exchange client to use for requests
	 */
	public OAuth2ApiClient(final OAuth2ClientRegistration clientRegistration, final OAuth2ProviderDetails providerDetails,
			final PrivateKey privateKey, final JwsAlgorithm signingAlgorithm, final ExchangeClient httpExchangeClient) {
		this(clientRegistration, providerDetails, httpExchangeClient);
		this.privateKey = privateKey;
		this.signingAlgorithm = signingAlgorithm;
	}

	/**
	 * Constructs a new OAuth2 API client with the specified configurations.
	 *
	 * @param clientRegistration the OAuth2 client registration details
	 * @param providerDetails the OAuth2 provider configuration details
	 * @param privateKey the private key used when authorizing with {@code private_key_jwt}
	 * @param signingAlgorithm the signing algorithm used to sign the client assertion
	 * @param exchangeClientBuilder the HTTP exchange client to use for requests
	 */
	public OAuth2ApiClient(final OAuth2ClientRegistration clientRegistration, final OAuth2ProviderDetails providerDetails,
			final PrivateKey privateKey, final JwsAlgorithm signingAlgorithm, final ExchangeClientBuilder exchangeClientBuilder) {
		this(clientRegistration, providerDetails, exchangeClientBuilder);
		this.privateKey = privateKey;
		this.signingAlgorithm = signingAlgorithm;
	}

	/**
	 * Retrieves a standard authentication token.
	 *
	 * @return the authentication token, or null if the request fails
	 */
	@Override
	public AuthenticationToken getAuthenticationToken() {
		return getAuthenticationToken(clientRegistration.getClientAuthenticationMethod());
	}

	/**
	 * Retrieves a standard authentication token.
	 *
	 * @param method OAuth2 client authentication method
	 * @return the authentication token, or null if the request fails
	 */
	public AuthenticationToken getAuthenticationToken(final ClientAuthenticationMethod method) {
		AuthorizationGrantType grantType = clientRegistration.getAuthorizationGrantType();
		if (AuthorizationGrantType.CLIENT_CREDENTIALS != grantType) {
			throw new UnsupportedOperationException("Unsupported authorization grant type: " + grantType);
		}
		return switch (method) {
			case CLIENT_SECRET_BASIC -> getTokenWithClientSecretBasic();
			case CLIENT_SECRET_POST -> getTokenWithClientSecretPost();
			case CLIENT_SECRET_JWT -> getTokenWithClientSecretJwt();
			case PRIVATE_KEY_JWT -> getTokenWithPrivateKeyJwt();
			default -> throw new UnsupportedOperationException("Unsupported client authentication method: " + method);
		};
	}

	/**
	 * Returns the authentication token with {@link ClientAuthenticationMethod#CLIENT_SECRET_BASIC} method.
	 *
	 * @return the authentication token with `client_secret_basic` method
	 */
	private AuthenticationToken getTokenWithClientSecretBasic() {
		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Parameter.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Parameter.EXPIRES_IN, OAuth2Parameter.Default.EXPIRES_IN.toSeconds()));

		return client()
				.http()
				.post()
				.url(providerDetails.getTokenUri())
				.body(RequestParameters.asString(RequestParameters.encode(params)))
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED)
				.header(HttpHeader.AUTHORIZATION, clientRegistration.getAuthorizationHeaderValue(HttpAuthScheme.BASIC))
				.retrieve(AuthenticationToken.class)
				.orRethrow(AuthenticationException::new);
	}

	/**
	 * Returns the authentication token with {@link ClientAuthenticationMethod#CLIENT_SECRET_POST} method.
	 *
	 * @return the authentication token with `client_secret_post` method
	 */
	private AuthenticationToken getTokenWithClientSecretPost() {
		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Parameter.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Parameter.EXPIRES_IN, OAuth2Parameter.Default.EXPIRES_IN.toSeconds()),
				parameter(OAuth2Parameter.CLIENT_ID, clientRegistration.getClientId()),
				parameter(OAuth2Parameter.CLIENT_SECRET, clientRegistration.getClientSecret()));

		return client()
				.http()
				.post()
				.url(providerDetails.getTokenUri())
				.body(RequestParameters.asString(RequestParameters.encode(params)))
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED)
				.retrieve(AuthenticationToken.class)
				.orRethrow(AuthenticationException::new);
	}

	/**
	 * Returns the authentication token with {@link ClientAuthenticationMethod#CLIENT_SECRET_JWT}.
	 *
	 * @return the authentication token with `client_secret_jwt` method
	 */
	private AuthenticationToken getTokenWithClientSecretJwt() {
		String clientAssertion = buildClientAssertionHmac(
				clientRegistration.getClientId(),
				providerDetails.getTokenUri(),
				clientRegistration.getClientSecret());

		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Parameter.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Parameter.CLIENT_ASSERTION_TYPE, "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"),
				parameter(OAuth2Parameter.CLIENT_ASSERTION, clientAssertion),
				parameter(OAuth2Parameter.EXPIRES_IN, OAuth2Parameter.Default.EXPIRES_IN.toSeconds()));

		return client()
				.http()
				.post()
				.url(providerDetails.getTokenUri())
				.body(RequestParameters.asString(RequestParameters.encode(params)))
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED)
				.retrieve(AuthenticationToken.class)
				.orRethrow(AuthenticationException::new);
	}

	/**
	 * Returns the authentication token with {@link ClientAuthenticationMethod#PRIVATE_KEY_JWT}.
	 *
	 * @return the authentication token with `private_key_jwt`.
	 */
	private AuthenticationToken getTokenWithPrivateKeyJwt() {
		// build a JWT signed with client private key (RSA or EC)
		String clientAssertion = buildClientAssertionPrivateKey(
				clientRegistration.getClientId(),
				providerDetails.getTokenUri(),
				privateKey,
				signingAlgorithm);

		Map<String, String> params = RequestParameters.of(
				parameter(OAuth2Parameter.GRANT_TYPE, clientRegistration.getAuthorizationGrantType()),
				parameter(OAuth2Parameter.CLIENT_ASSERTION_TYPE, "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"),
				parameter(OAuth2Parameter.CLIENT_ASSERTION, clientAssertion),
				parameter(OAuth2Parameter.EXPIRES_IN, OAuth2Parameter.Default.EXPIRES_IN.toSeconds()));

		return client()
				.http()
				.post()
				.url(providerDetails.getTokenUri())
				.body(RequestParameters.asString(RequestParameters.encode(params)))
				.header(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED)
				.retrieve(AuthenticationToken.class)
				.orRethrow(AuthenticationException::new);
	}

	/**
	 * Returns the client assertion HMAC.
	 *
	 * @param clientId the client ID
	 * @param tokenEndpoint the token end point
	 * @param clientSecret the client secret
	 * @return the client assertion HMAC
	 */
	public static String buildClientAssertionHmac(final String clientId, final String tokenEndpoint, final String clientSecret) {
		Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
		Map<String, Object> claims = defaultClaims(clientId, tokenEndpoint);

		String headerJson = Strings.removeAllWhitespace(JsonBuilder.toJson(header));
		String payloadJson = Strings.removeAllWhitespace(JsonBuilder.toJson(claims));

		String headerB64 = BASE64_URL_ENCODER.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
		String payloadB64 = BASE64_URL_ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
		String signingInput = headerB64 + "." + payloadB64;

		byte[] signature = MessageDigestAlgorithm.SHA256.hmac(
				clientSecret.getBytes(StandardCharsets.UTF_8), signingInput.getBytes(StandardCharsets.UTF_8));
		return signingInput + "." + BASE64_URL_ENCODER.encodeToString(signature);
	}

	/**
	 * Returns the client assertion private key.
	 *
	 * @param clientId the client ID
	 * @param tokenEndpoint the token end point
	 * @param privateKey the private key
	 * @param algorithm the algorithm to sign with
	 * @return the client assertion private key
	 */
	public static String buildClientAssertionPrivateKey(final String clientId, final String tokenEndpoint, final PrivateKey privateKey,
			final JwsAlgorithm algorithm) {
		Map<String, Object> header = Map.of("alg", algorithm, "typ", "JWT");
		Map<String, Object> claims = defaultClaims(clientId, tokenEndpoint);

		String headerJson = Strings.removeAllWhitespace(JsonBuilder.toJson(header));
		String payloadJson = Strings.removeAllWhitespace(JsonBuilder.toJson(claims));

		String headerB64 = BASE64_URL_ENCODER.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
		String payloadB64 = BASE64_URL_ENCODER.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
		String signingInput = headerB64 + "." + payloadB64;

		byte[] signature = Signer.sign(privateKey, algorithm, signingInput.getBytes(StandardCharsets.UTF_8));

		return signingInput + "." + BASE64_URL_ENCODER.encodeToString(signature);
	}

	/**
	 * Returns the JWT default claims map.
	 *
	 * @param clientId the client ID
	 * @param tokenEndpoint the token end point
	 * @return the JWT default claims map
	 */
	private static Map<String, Object> defaultClaims(final String clientId, final String tokenEndpoint) {
		Duration defaultExpiration = Duration.ofMinutes(5);
		Instant now = Instant.now();
		return Map.of(
				"iss", clientId,
				"sub", clientId,
				"aud", tokenEndpoint,
				"jti", UUID.randomUUID().toString(),
				"iat", now.getEpochSecond(),
				"exp", now.plusSeconds(defaultExpiration.toSeconds()).getEpochSecond());
	}
}
