package org.apiphany.security.oauth2;

import java.time.Duration;

import org.apiphany.security.AuthenticationToken;
import org.morphix.reflection.Constructors;

/**
 * All {@link OAuth2TokenProvider} configurable properties.
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2TokenProviderProperties {

	/**
	 * Defaults name space class.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Default {

		/**
		 * Duration for error margin when checking token expiration - 5 seconds.
		 */
		public static final Duration EXPIRATION_ERROR_MARGIN = AuthenticationToken.EXPIRATION_ERROR_MARGIN;

		/**
		 * The minimum refresh interval when the token could not be retrieved.
		 */
		public static final Duration MIN_REFRESH_INTERVAL = Duration.ofMillis(500);

		/**
		 * The maximum number of attempts the {@link OAuth2TokenProvider#close()} method tries to close the scheduled task.
		 */
		public static final int MAX_TASK_CLOSE_ATTEMPTS = 10;

		/**
		 * The interval between close attempts when closing the scheduled task.
		 */
		public static final Duration CLOSE_TASK_RETRY_INTERVAL = Duration.ofMillis(200);

		/**
		 * Hide constructor.
		 */
		private Default() {
			throw Constructors.unsupportedOperationException();
		}
	}

	/**
	 * The error margin for new token checking. When checking for new token the provider will try retrieving a new token at:
	 *
	 * <pre>
	 * token.expiration - expirationErrorMargin
	 * </pre>
	 */
	private Duration expirationErrorMargin = Default.EXPIRATION_ERROR_MARGIN;

	/**
	 * The minimum refresh interval is the interval for new token retrieval when the previous attempt failed.
	 */
	private Duration minRefreshInterval = Default.MIN_REFRESH_INTERVAL;

	/**
	 * The maximum attempts to close the scheduled task when calling {@link OAuth2TokenProvider#close()}.
	 */
	private int maxTaskCloseAttempts = Default.MAX_TASK_CLOSE_ATTEMPTS;

	/**
	 * The interval between close attempts when closing the scheduled task.
	 */
	private Duration closeTaskRetryInterval = Default.CLOSE_TASK_RETRY_INTERVAL;

	/**
	 * Default constructor.
	 */
	public OAuth2TokenProviderProperties() {
		// empty
	}

	/**
	 * Returns a new OAuth2 token provider properties object with all the defaults.
	 *
	 * @return a new properties object with all the defaults
	 */
	public static OAuth2TokenProviderProperties defaults() {
		return new OAuth2TokenProviderProperties();
	}

	/**
	 * Returns the expiration error margin.
	 *
	 * @return the expiration error margin
	 */
	public Duration getExpirationErrorMargin() {
		return expirationErrorMargin;
	}

	/**
	 * Sets the expiration error margin.
	 *
	 * @param expirationErrorMargin the duration representing the error margin
	 */
	public void setExpirationErrorMargin(final Duration expirationErrorMargin) {
		this.expirationErrorMargin = expirationErrorMargin;
	}

	/**
	 * Returns the minimum refresh interval.
	 *
	 * @return the minimum refresh interval
	 */
	public Duration getMinRefreshInterval() {
		return minRefreshInterval;
	}

	/**
	 * Sets the minimum refresh interval.
	 *
	 * @param minRefreshInterval the interval to set
	 */
	public void setMinRefreshInterval(final Duration minRefreshInterval) {
		this.minRefreshInterval = minRefreshInterval;
	}

	/**
	 * Returns the maximum close attempts.
	 *
	 * @return the maximum close attempts
	 */
	public int getMaxTaskCloseAttempts() {
		return maxTaskCloseAttempts;
	}

	/**
	 * Sets the maximum close attempts.
	 *
	 * @param maxCloseAttempts the maximum close attempts to set
	 */
	public void setMaxTaskCloseAttempts(final int maxCloseAttempts) {
		this.maxTaskCloseAttempts = maxCloseAttempts;
	}

	/**
	 * Returns the close retry interval.
	 *
	 * @return the close retry interval
	 */
	public Duration getCloseTaskRetryInterval() {
		return closeTaskRetryInterval;
	}

	/**
	 * Sets the close retry interval.
	 *
	 * @param closeRetryInterval the close retry interval to set
	 */
	public void setCloseTaskRetryInterval(final Duration closeRetryInterval) {
		this.closeTaskRetryInterval = closeRetryInterval;
	}
}
