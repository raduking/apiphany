package org.apiphany.security.oauth2;

import static org.apiphany.test.Assertions.assertDefaultConstructorThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;

import org.apiphany.security.AuthenticationToken;
import org.junit.jupiter.api.Test;
import org.morphix.reflection.Constructors;

/**
 * Test class for {@link OAuth2TokenProviderProperties}.
 *
 * @author Radu Sebastian LAZIN
 */
class OAuth2TokenProviderPropertiesTest {

	private static final Duration EXPIRATION_ERROR_MARGIN = AuthenticationToken.EXPIRATION_ERROR_MARGIN;
	private static final Duration MIN_REFRESH_INTERVAL = Duration.ofMillis(500);
	private static final int MAX_TASK_CLOSE_ATTEMPTS = 10;
	private static final Duration CLOSE_TASK_RETRY_INTERVAL = Duration.ofMillis(200);

	@Test
	void shouldNotInstantiateDefaultClass() {
		UnsupportedOperationException unsupportedOperationException =
				assertDefaultConstructorThrows(OAuth2TokenProviderProperties.Default.class);
		assertThat(unsupportedOperationException.getMessage(), equalTo(Constructors.MESSAGE_THIS_CLASS_SHOULD_NOT_BE_INSTANTIATED));
	}

	@Test
	void shouldInitializeWithDefaultPropertiesOnNewInstance() {
		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();

		assertThat(properties.getExpirationErrorMargin(), equalTo(OAuth2TokenProviderProperties.Default.EXPIRATION_ERROR_MARGIN));
		assertThat(properties.getMinRefreshInterval(), equalTo(OAuth2TokenProviderProperties.Default.MIN_REFRESH_INTERVAL));
		assertThat(properties.getMaxTaskCloseAttempts(), equalTo(OAuth2TokenProviderProperties.Default.MAX_TASK_CLOSE_ATTEMPTS));
		assertThat(properties.getCloseTaskRetryInterval(), equalTo(OAuth2TokenProviderProperties.Default.CLOSE_TASK_RETRY_INTERVAL));
	}

	@Test
	void shouldSetAndGetPropertiesCorrectly() {
		OAuth2TokenProviderProperties properties = new OAuth2TokenProviderProperties();

		properties.setExpirationErrorMargin(Duration.ofSeconds(120));
		properties.setMinRefreshInterval(Duration.ofSeconds(300));
		properties.setMaxTaskCloseAttempts(5);
		properties.setCloseTaskRetryInterval(Duration.ofSeconds(10));

		assertThat(properties.getExpirationErrorMargin(), equalTo(Duration.ofSeconds(120)));
		assertThat(properties.getMinRefreshInterval(), equalTo(Duration.ofSeconds(300)));
		assertThat(properties.getMaxTaskCloseAttempts(), equalTo(5));
		assertThat(properties.getCloseTaskRetryInterval(), equalTo(Duration.ofSeconds(10)));
	}

	@Test
	void shouldHaveTheProvidedDefaults() {
		assertThat(OAuth2TokenProviderProperties.Default.EXPIRATION_ERROR_MARGIN, equalTo(EXPIRATION_ERROR_MARGIN));
		assertThat(OAuth2TokenProviderProperties.Default.MIN_REFRESH_INTERVAL, equalTo(MIN_REFRESH_INTERVAL));
		assertThat(OAuth2TokenProviderProperties.Default.MAX_TASK_CLOSE_ATTEMPTS, equalTo(MAX_TASK_CLOSE_ATTEMPTS));
		assertThat(OAuth2TokenProviderProperties.Default.CLOSE_TASK_RETRY_INTERVAL, equalTo(CLOSE_TASK_RETRY_INTERVAL));
	}
}
