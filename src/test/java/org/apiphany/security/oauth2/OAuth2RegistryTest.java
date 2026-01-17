package org.apiphany.security.oauth2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apiphany.security.AuthenticationToken;
import org.apiphany.security.AuthenticationTokenProvider;
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

	private static final String TOKEN_URI = "http://localhost:1234/token";

	private static final int EXPIRES_IN = 300;
	private static final Instant DEFAULT_EXPIRATION = Instant.now();

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

		OAuth2Properties properties = new OAuth2Properties();
		properties.setRegistration(registration);
		properties.setProvider(provider);

		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(2));
		assertThat(registry.get(REGISTRATION_1).getClientRegistration().getProvider(), equalTo(PROVIDER_1));
		assertThat(registry.get(REGISTRATION_2).getClientRegistration().getProvider(), equalTo(PROVIDER_2));
	}

	@Test
	void shouldBuildEmptyRegistryFromEmptyProperties() {
		OAuth2Properties properties = new OAuth2Properties();

		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(0));
	}

	@Test
	void shouldBuildEmptyRegistryFromPropertiesWithoutProvider() {
		OAuth2ClientRegistration registration1 = buildRegistration(CLIENT_1, PROVIDER_1);
		Map<String, OAuth2ClientRegistration> registration = Map.of(
				REGISTRATION_1, registration1);
		OAuth2Properties properties = new OAuth2Properties();
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

		OAuth2Properties properties = new OAuth2Properties();
		properties.setRegistration(registration);
		properties.setProvider(provider);

		OAuth2Registry registry = OAuth2Registry.of(properties);

		assertThat(registry.entries(), hasSize(1));
		assertThat(registry.get("_"), nullValue());
	}

	@Test
	void shouldBuildTokenProvidersFromRegistry() throws Exception {
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

		OAuth2Properties properties = new OAuth2Properties();
		properties.setRegistration(registration);
		properties.setProvider(provider);

		OAuth2Registry registry = OAuth2Registry.of(properties);

		List<OAuth2TokenProvider> tokenProviders = null;
		try {
			tokenProviders = registry.tokenProviders((cr, cp) -> tokenClient());
		} catch (Exception e) {
			fail("Should not reach this code");
		} finally {
			if (null != tokenProviders) {
				for (OAuth2TokenProvider tokenProvider : tokenProviders) {
					tokenProvider.close();
				}
			}
		}
	}

	@Test
	void shouldBuildEmptyRegistryFromNullProperties() {
		OAuth2Registry registry = OAuth2Registry.of();

		assertThat(registry.entries(), hasSize(0));
	}

	private static AuthenticationTokenProvider tokenClient() {
		return () -> createToken("token-" + new Random().nextInt());
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

	private static AuthenticationToken createToken(final String token) {
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setAccessToken(token);
		authenticationToken.setExpiresIn(EXPIRES_IN);
		authenticationToken.setExpiration(DEFAULT_EXPIRATION.plusSeconds(EXPIRES_IN));
		return authenticationToken;
	}

}
