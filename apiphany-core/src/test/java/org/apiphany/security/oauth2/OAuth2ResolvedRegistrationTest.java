package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
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
				Map.of(PROVIDER, createProvider("https://localhost/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, null);

		assertNull(result);
	}

	@Test
	void shouldResolveSingleRegistrationWhenRegistrationNameMissing() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration("client-id", PROVIDER)),
				Map.of(PROVIDER, createProvider("https://localhost/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, null);

		assertThat(result, notNullValue());
		assertThat(result.getClientRegistrationName(), equalTo(REGISTRATION));
	}

	@Test
	void shouldReturnNullWhenResolvedRegistrationNameIsEmpty() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of("", createRegistration("client-id", PROVIDER)),
				Map.of(PROVIDER, createProvider("https://localhost/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, null);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenProvidersAreMissing() {
		OAuth2Properties properties = OAuth2Properties.of();
		properties.setRegistration(Map.of(REGISTRATION, createRegistration("client-id", PROVIDER)));
		properties.setProvider(Map.of());

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenRegistrationDoesNotExist() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of("other", createRegistration("client-id", PROVIDER)),
				Map.of(PROVIDER, createProvider("https://localhost/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenClientIdIsMissing() {
		OAuth2ClientRegistration registration = createRegistration("client-id", PROVIDER);
		registration.setClientId(null);

		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, registration),
				Map.of(PROVIDER, createProvider("https://localhost/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenClientSecretIsMissing() {
		OAuth2ClientRegistration registration = createRegistration("client-id", PROVIDER);
		registration.setClientSecret(null);

		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, registration),
				Map.of(PROVIDER, createProvider("https://localhost/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenProviderDetailsAreMissing() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration("client-id", "unknown-provider")),
				Map.of(PROVIDER, createProvider("https://localhost/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenInsecureTokenUriIsNotAllowed() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration("client-id", PROVIDER)),
				Map.of(PROVIDER, createProvider("http://localhost:8080/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldReturnNullWhenInsecureTokenUriIsAllowedButGloballyForbidden() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration("client-id", PROVIDER)),
				Map.of(PROVIDER, createProvider("http://localhost:8080/token", true)));
		properties.setForbidInsecureTokenUri(true);

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertNull(result);
	}

	@Test
	void shouldResolveWhenInsecureTokenUriIsAllowedAndNotGloballyForbidden() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration("client-id", PROVIDER)),
				Map.of(PROVIDER, createProvider("http://localhost:8080/token", true)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertThat(result, notNullValue());
		assertThat(result.getClientRegistrationName(), equalTo(REGISTRATION));
	}

	@Test
	void shouldResolveWhenTokenUriIsSecure() {
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration("client-id", PROVIDER)),
				Map.of(PROVIDER, createProvider("https://localhost:8080/token", false)));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertThat(result, notNullValue());
	}

	@Test
	void shouldResolveWhenTokenUriIsEmpty() {
		OAuth2ProviderDetails providerDetails = createProvider("", false);
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION, createRegistration("client-id", PROVIDER)),
				Map.of(PROVIDER, providerDetails));

		OAuth2ResolvedRegistration result = OAuth2ResolvedRegistration.of(properties, REGISTRATION);

		assertThat(result, notNullValue());
	}

	private static OAuth2ClientRegistration createRegistration(final String clientId, final String provider) {
		OAuth2ClientRegistration registration = new OAuth2ClientRegistration();
		registration.setClientId(clientId);
		registration.setClientSecret("client-secret");
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
