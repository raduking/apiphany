package org.apiphany.security.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.apiphany.security.AuthenticationTokenProvider;
import org.junit.jupiter.api.Test;
import org.morphix.lang.function.ExecutionWrapper;

/**
 * Test class for {@link OAuth2TokenProviderSpec}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2TokenProviderSpecTest {

	private static final String TEST_REGISTRATION = "test-registration";
	private static final String TEST_PROVIDER = "test-provider";

	@Test
	@SuppressWarnings("resource")
	void shouldCreateWithDefaults() throws Exception {
		OAuth2TokenProviderSpec spec = OAuth2TokenProviderSpec.builder().build();

		OAuth2TokenClientSupplier defaultTokenClientSupplier = spec.getTokenClientSupplier();
		ExecutionWrapper<Void> defaultUpdateTokenWrapper = spec.getUpdateTokenWrapper();

		try {
			assertNotNull(spec.getTokenProviderProperties());
			assertNotNull(spec.getTokenRefreshScheduler());
			assertNotNull(defaultTokenClientSupplier);
			assertNotNull(spec.getDefaultExpirationSupplier());
			assertEquals(ExecutionWrapper.EMPTY, defaultUpdateTokenWrapper);

			AuthenticationTokenProvider tokenProvider = defaultTokenClientSupplier.get(null, null);

			assertNull(tokenProvider);

			assertTrue(spec.getTokenRefreshScheduler().isManaged());
		} finally {
			spec.getTokenRefreshScheduler().closeIfManaged();
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldCreateWithCustomValues() {
		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();

		OAuth2Properties oauth2properties = new OAuth2Properties();

		OAuth2ClientRegistration clientRegistration = new OAuth2ClientRegistration();
		clientRegistration.setClientId("test-client-id");
		clientRegistration.setClientSecret("test-client-secret");
		clientRegistration.setProvider(TEST_PROVIDER);

		OAuth2ProviderDetails providerDetails = new OAuth2ProviderDetails();

		oauth2properties.setRegistration(Map.of(TEST_REGISTRATION, clientRegistration));
		oauth2properties.setProvider(Map.of(TEST_PROVIDER, providerDetails));

		OAuth2TokenClientSupplier tokenClientSupplier = (cr, pd) -> null;
		var defaultExpirationSupplier = (Supplier<Instant>) Instant::now;
		ExecutionWrapper<Void> wrapper = s -> null;

		try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
			OAuth2TokenProviderSpec config = OAuth2TokenProviderSpec.builder()
					.properties(properties)
					.registration(oauth2properties)
					.tokenRefreshScheduler(scheduler)
					.tokenClientSupplier(tokenClientSupplier)
					.defaultExpirationSupplier(defaultExpirationSupplier)
					.updateTokenWrapper(wrapper)
					.build();

			assertEquals(properties, config.getTokenProviderProperties());
			assertEquals(scheduler, config.getTokenRefreshScheduler().unwrap());
			assertEquals(tokenClientSupplier, config.getTokenClientSupplier());
			assertEquals(defaultExpirationSupplier, config.getDefaultExpirationSupplier());
			assertEquals(wrapper, config.getUpdateTokenWrapper());
			assertNotNull(config.getResolvedRegistration());
		}
	}

	@Test
	@SuppressWarnings("resource")
	void shouldReturnNullAuthenticationTokenProviderWhenUsingDefaultSupplier() throws Exception {
		OAuth2TokenProviderSpec spec = OAuth2TokenProviderSpec.builder().build();

		var tokenProvider = spec.getTokenClientSupplier().get(
				new OAuth2ClientRegistration(),
				new OAuth2ProviderDetails());

		assertNull(tokenProvider);

		spec.getTokenRefreshScheduler().closeIfManaged();
	}
}
