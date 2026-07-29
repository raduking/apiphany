package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2ResolvedRegistration}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2ResolvedRegistrationTest {

	private static final String REGISTRATION = "my-client";
	private static final String PROVIDER = "my-provider";
	private static final String CLIENT_ID = "client-id";
	private static final String CLIENT_SECRET = "client-secret";
	private static final String UNKNOWN_PROVIDER = "unknown-provider";
	private static final String SECURE_TOKEN_URI = "https://localhost:8080/token";
	private static final String INSECURE_TOKEN_URI = "http://localhost:8080/token";
	private static final String SECURE_TOKEN_URI_NO_PORT = "https://localhost/token";

	@Test
	void shouldReturnNullWhenPropertiesAreNull() {
		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(null, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenRegistrationsAreMissing() {
		OAuth2Properties properties = OAuth2Properties.of();
		properties.setRegistration(Map.of());

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenRegistrationNameMissingAndMultipleRegistrationsExist() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(
						"a", createRegistration("a", PROVIDER),
						"b", createRegistration("b", PROVIDER)),
				Map.of(PROVIDER, createProvider(SECURE_TOKEN_URI_NO_PORT, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, null);

		assertNull(result);
	}

	@Test
	void shouldResolveSingleRegistrationWhenRegistrationNameMissing() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration(CLIENT_ID, PROVIDER)),
				Map.of(PROVIDER, createProvider(SECURE_TOKEN_URI_NO_PORT, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, null);

		assertThat(result, notNullValue());
		assertThat(result.getClientRegistrationName(), equalTo(REGISTRATION));
	}

	@Test
	void shouldReturnNullWhenResolvedRegistrationNameIsEmpty() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of("", createRegistration(CLIENT_ID, PROVIDER)),
				Map.of(PROVIDER, createProvider(SECURE_TOKEN_URI_NO_PORT, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, null);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenProvidersAreMissing() {
		OAuth2Properties properties = OAuth2Properties.of();
		properties.setRegistration(Map.of(REGISTRATION, createRegistration(CLIENT_ID, PROVIDER)));
		properties.setProvider(Map.of());

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenRegistrationDoesNotExist() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of("other", createRegistration(CLIENT_ID, PROVIDER)),
				Map.of(PROVIDER, createProvider(SECURE_TOKEN_URI_NO_PORT, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenClientIdIsMissing() {
		OAuth2ClientRegistration registration = createRegistration(CLIENT_ID, PROVIDER);
		registration.setClientId(null);

		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, registration),
				Map.of(PROVIDER, createProvider(SECURE_TOKEN_URI_NO_PORT, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenClientSecretIsMissing() {
		OAuth2ClientRegistration registration = createRegistration(CLIENT_ID, PROVIDER);
		registration.setClientSecret(null);

		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, registration),
				Map.of(PROVIDER, createProvider(SECURE_TOKEN_URI_NO_PORT, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenProviderDetailsAreMissing() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration(CLIENT_ID, UNKNOWN_PROVIDER)),
				Map.of(PROVIDER, createProvider(SECURE_TOKEN_URI_NO_PORT, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenInsecureTokenUriIsNotAllowed() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration(CLIENT_ID, PROVIDER)),
				Map.of(PROVIDER, createProvider(INSECURE_TOKEN_URI, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenInsecureTokenUriIsAllowedButGloballyForbidden() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration(CLIENT_ID, PROVIDER)),
				Map.of(PROVIDER, createProvider(INSECURE_TOKEN_URI, true)));
		properties.setForbidInsecureTokenUri(true);

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldResolveWhenInsecureTokenUriIsAllowedAndNotGloballyForbidden() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration(CLIENT_ID, PROVIDER)),
				Map.of(PROVIDER, createProvider(INSECURE_TOKEN_URI, true)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertThat(result, notNullValue());
		assertThat(result.getClientRegistrationName(), equalTo(REGISTRATION));
	}

	@Test
	void shouldResolveWhenTokenUriIsSecure() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration(CLIENT_ID, PROVIDER)),
				Map.of(PROVIDER, createProvider(SECURE_TOKEN_URI, false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldResolveWhenTokenUriIsEmpty() {
		OAuth2ProviderDetails providerDetails = createProvider("", false);
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration(CLIENT_ID, PROVIDER)),
				Map.of(PROVIDER, providerDetails));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldReturnNullWhenInsecureTokenUriIsNotAllowedForDirectFactory() {
		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(
				REGISTRATION,
				createRegistration(CLIENT_ID, PROVIDER),
				createProvider(INSECURE_TOKEN_URI, false));

		assertNull(result);
	}

	@Test
	void shouldResolveWhenInsecureTokenUriIsAllowedForDirectFactory() {
		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(
				REGISTRATION,
				createRegistration(CLIENT_ID, PROVIDER),
				createProvider(INSECURE_TOKEN_URI, true));

		assertThat(result, notNullValue());
		assertThat(result.getClientRegistrationName(), equalTo(REGISTRATION));
	}

	@Test
	void shouldResolveWhenSecureTokenUriForDirectFactory() {
		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(
				REGISTRATION,
				createRegistration(CLIENT_ID, PROVIDER),
				createProvider(SECURE_TOKEN_URI, false));

		assertThat(result, notNullValue());
	}

	@Test
	void shouldReturnAllFieldsWhenResolved() {
		OAuth2ClientRegistration registration = createRegistration(CLIENT_ID, PROVIDER);
		OAuth2ProviderDetails provider = createProvider(SECURE_TOKEN_URI, false);

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(REGISTRATION, registration, provider);

		assertThat(result, notNullValue());
		assertThat(result.getClientRegistrationName(), equalTo(REGISTRATION));
		assertThat(result.getClientRegistration(), equalTo(registration));
		assertThat(result.getProviderDetails(), equalTo(provider));
	}

	@Test
	void shouldResolveWhenProviderDetailsIsNullForDirectFactory() {
		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(
				REGISTRATION,
				createRegistration(CLIENT_ID, PROVIDER),
				null);

		assertThat(result, notNullValue());
		assertThat(result.getClientRegistrationName(), equalTo(REGISTRATION));
		assertThat(result.getClientRegistration(), notNullValue());
		assertThat(result.getProviderDetails(), nullValue());
	}

	@Test
	void shouldUseUnknownRegistrationNameWhenNameIsNullForDirectFactory() {
		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(
				null,
				createRegistration(CLIENT_ID, PROVIDER),
				createProvider(SECURE_TOKEN_URI, false));

		assertThat(result, notNullValue());
		assertThat(result.getClientRegistrationName(), equalTo(OAuth2ResolvedRegistration.UNKNOWN_REGISTRATION_NAME));
		assertThat(result.getClientRegistration(), notNullValue());
		assertThat(result.getProviderDetails(), notNullValue());
	}

	private static OAuth2ClientRegistration createRegistration(final String clientId, final String provider) {
		OAuth2ClientRegistration registration = new OAuth2ClientRegistration();
		registration.setClientId(clientId);
		registration.setClientSecret(CLIENT_SECRET);
		registration.setProvider(provider);
		return registration;
	}

	private static OAuth2ProviderDetails createProvider(final String tokenUri, final boolean allowInsecureTokenUri) {
		OAuth2ProviderDetails providerDetails = new OAuth2ProviderDetails();
		providerDetails.setTokenUri(tokenUri);
		providerDetails.setAllowInsecureTokenUri(allowInsecureTokenUri);
		return providerDetails;
	}
}
