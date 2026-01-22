package org.apiphany.security.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link OAuth2TokenProviderSpec}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2TokenProviderSpecTest {

	@SuppressWarnings("resource")
	@Test
	void shouldCreateWithDefaults() {
		OAuth2TokenProviderSpec config = OAuth2TokenProviderSpec.builder().build();

		assertNotNull(config.properties());
		assertNotNull(config.tokenRefreshScheduler());
		assertNotNull(config.tokenClientSupplier());
		assertNotNull(config.defaultExpirationSupplier());
	}

	@SuppressWarnings("resource")
	@Test
	void shouldCreateWithCustomValues() {
		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();
		OAuth2ResolvedRegistration registration = OAuth2ResolvedRegistration.of(
				"test-registration",
				new OAuth2ClientRegistration(),
				new OAuth2ProviderDetails());
		OAuth2TokenClientSupplier tokenClientSupplier = (cr, pd) -> null;
		var defaultExpirationSupplier = (Supplier<Instant>) Instant::now;

		try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
			OAuth2TokenProviderSpec config = OAuth2TokenProviderSpec.builder()
					.properties(properties)
					.registration(registration)
					.tokenRefreshScheduler(scheduler)
					.tokenClientSupplier(tokenClientSupplier)
					.defaultExpirationSupplier(defaultExpirationSupplier)
					.build();

			assertEquals(properties, config.properties());
			assertEquals(scheduler, config.tokenRefreshScheduler());
			assertEquals(tokenClientSupplier, config.tokenClientSupplier());
			assertEquals(defaultExpirationSupplier, config.defaultExpirationSupplier());
		}
	}

	@Test
	void shouldReturnNullAuthenticationTokenProviderWhenUsingDefaultSupplier() {
		OAuth2TokenProviderSpec config = OAuth2TokenProviderSpec.builder().build();

		var tokenProvider = config.tokenClientSupplier().get(
				new OAuth2ClientRegistration(),
				new OAuth2ProviderDetails());

		assertEquals(null, tokenProvider);
	}
}
