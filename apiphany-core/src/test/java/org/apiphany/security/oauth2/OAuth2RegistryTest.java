package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2Registry}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2RegistryTest {

	private static final String REGISTRATION_1 = "registration1";
	private static final String REGISTRATION_2 = "registration2";

	private static final String PROVIDER_1 = "provider1";
	private static final String PROVIDER_2 = "provider2";

	private static final String CLIENT_1 = "client1";
	private static final String CLIENT_2 = "client2";

	private static final String TOKEN_URI = "https://localhost:1234/token";

	@Test
	void shouldBuildValidRegistry() {
		OAuth2ClientRegistration registration1 = buildRegistration(CLIENT_1, PROVIDER_1);
		OAuth2ClientRegistration registration2 = buildRegistration(CLIENT_2, PROVIDER_2);
		Map<String, OAuth2ClientRegistration> registration = Map.of(
				REGISTRATION_1, registration1,
				REGISTRATION_2, registration2);

		OAuth2ProviderDetails provider1 = buildProvider(CLIENT_1);
		OAuth2ProviderDetails provider2 = buildProvider(CLIENT_2);
		Map<String, OAuth2ProviderDetails> provider = Map.of(
				PROVIDER_1, provider1,
				PROVIDER_2, provider2);

		OAuth2Properties properties = OAuth2Properties.of(registration, provider);
		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(2));
		assertThat(registry.get(REGISTRATION_1).getClientRegistration().getProvider(), equalTo(PROVIDER_1));
		assertThat(registry.get(REGISTRATION_2).getClientRegistration().getProvider(), equalTo(PROVIDER_2));
	}

	@Test
	void shouldBuildEmptyRegistryFromEmptyProperties() {
		OAuth2Properties properties = OAuth2Properties.of();

		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(0));
	}

	@Test
	void shouldBuildEmptyRegistryFromPropertiesWithoutProvider() {
		OAuth2ClientRegistration registration1 = buildRegistration(CLIENT_1, PROVIDER_1);
		Map<String, OAuth2ClientRegistration> registration = Map.of(
				REGISTRATION_1, registration1);
		OAuth2Properties properties = OAuth2Properties.of();
		properties.setRegistration(registration);

		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(0));
	}

	@Test
	void shouldReturnNullForUnknownClientRegistration() {
		OAuth2ClientRegistration registration1 = buildRegistration(CLIENT_1, PROVIDER_1);
		Map<String, OAuth2ClientRegistration> registration = Map.of(
				REGISTRATION_1, registration1);
		OAuth2ProviderDetails provider1 = buildProvider(CLIENT_1);
		Map<String, OAuth2ProviderDetails> provider = Map.of(
				PROVIDER_1, provider1);

		OAuth2Properties properties = OAuth2Properties.of(registration, provider);
		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(1));
		assertThat(registry.get("_"), nullValue());
	}

	@Test
	void shouldBuildEmptyRegistryFromNullProperties() {
		OAuth2Registry registry = OAuth2Registry.of();

		assertThat(registry.entries(), hasSize(0));
	}

	@Test
	void shouldSkipRegistrationWhenInsecureTokenUriIsNotAllowed() {
		OAuth2ClientRegistration registration = buildRegistration(CLIENT_1, PROVIDER_1);
		OAuth2ProviderDetails provider = buildProvider("http://localhost:1234/token", false);
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION_1, registration),
				Map.of(PROVIDER_1, provider));

		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(0));
		assertThat(registry.get(REGISTRATION_1), nullValue());
	}

	@Test
	void shouldSkipRegistrationWhenInsecureTokenUriIsAllowedButGloballyForbidden() {
		OAuth2ClientRegistration registration = buildRegistration(CLIENT_1, PROVIDER_1);
		OAuth2ProviderDetails provider = buildProvider("http://localhost:1234/token", true);
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION_1, registration),
				Map.of(PROVIDER_1, provider));
		properties.setForbidInsecureTokenUri(true);

		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(0));
		assertThat(registry.get(REGISTRATION_1), nullValue());
	}

	@Test
	void shouldKeepRegistrationWhenInsecureTokenUriIsAllowedAndNotGloballyForbidden() {
		OAuth2ClientRegistration registration = buildRegistration(CLIENT_1, PROVIDER_1);
		OAuth2ProviderDetails provider = buildProvider("http://localhost:1234/token", true);
		OAuth2Properties properties = OAuth2Properties.of(
				Map.of(REGISTRATION_1, registration),
				Map.of(PROVIDER_1, provider));

		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(1));
		OAuth2ResolvedRegistration resolvedRegistration = registry.get(REGISTRATION_1);

		assertThat(resolvedRegistration, org.hamcrest.Matchers.notNullValue());
		assertThat(resolvedRegistration.getClientRegistrationName(), equalTo(REGISTRATION_1));
		assertThat(resolvedRegistration.getClientRegistration().getProvider(), equalTo(PROVIDER_1));
		assertThat(resolvedRegistration.getProviderDetails().getTokenUri(), equalTo("http://localhost:1234/token"));
	}

	private static OAuth2ClientRegistration buildRegistration(final String client, final String provider) {
		OAuth2ClientRegistration registration = new OAuth2ClientRegistration();
		registration.setClientId(client + "Id");
		registration.setClientSecret(client + "Secret");
		registration.setProvider(provider);
		return registration;
	}

	private static OAuth2ProviderDetails buildProvider(final String tokenUri) {
		OAuth2ProviderDetails provider = new OAuth2ProviderDetails();
		provider.setTokenUri(TOKEN_URI + "/" + tokenUri);
		return provider;
	}

	private static OAuth2ProviderDetails buildProvider(final String tokenUri, final boolean allowInsecureTokenUri) {
		OAuth2ProviderDetails provider = new OAuth2ProviderDetails();
		provider.setTokenUri(tokenUri);
		provider.setAllowInsecureTokenUri(allowInsecureTokenUri);
		return provider;
	}
}
