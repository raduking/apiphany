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
		 * The minimum refresh interval when the token could not be retrieved due to an error - 500 milliseconds.
		 */
		public static final Duration MIN_REFRESH_INTERVAL = Duration.ofMillis(500);

		/**
		 * The maximum refresh interval when token retrieval keeps failing - 30 seconds.
		 */
		public static final Duration MAX_REFRESH_INTERVAL = Duration.ofSeconds(30);

		/**
		 * The exponential delay multiplier for token refresh retries - 2.0.
		 */
		public static final double REFRESH_FAILURE_DELAY_MULTIPLIER = 2.0d;

		/**
		 * The maximum number of attempts {@link OAuth2TokenProvider#close()} tries to close the scheduled task - 10 attempts.
		 */
		public static final int MAX_TASK_CLOSE_ATTEMPTS = 10;

		/**
		 * The interval between close attempts when closing the scheduled task - 100 milliseconds.
		 */
		public static final Duration CLOSE_TASK_RETRY_INTERVAL = Duration.ofMillis(100);

		/**
		 * The timeout for the scheduler termination when closing the provider - 0 seconds. The scheduler will be shutdown
		 * immediately and the provider will not wait for the termination of the scheduler, which means that the provider will
		 * be closed even if there are still tasks running in the scheduler. This is because the provider is expected to be
		 * closed when the application is shutting down, and waiting for the termination of the scheduler could delay the
		 * shutdown process. If a different behavior is desired, this property can be set to a different value.
		 */
		public static final Duration SCHEDULER_TERMINATION_TIMEOUT = Duration.ZERO;

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
	 * The maximum refresh interval when token refresh retries are exponentially backed off.
	 */
	private Duration maxRefreshInterval = Default.MAX_REFRESH_INTERVAL;

	/**
	 * The exponential delay multiplier for token refresh failure retries.
	 */
	private Double refreshFailureDelayMultiplier = Default.REFRESH_FAILURE_DELAY_MULTIPLIER;

	/**
	 * The maximum attempts to close the scheduled task when calling {@link OAuth2TokenProvider#close()}.
	 */
	private Integer maxTaskCloseAttempts = Default.MAX_TASK_CLOSE_ATTEMPTS;

	/**
	 * The interval between close attempts when closing the scheduled task.
	 */
	private Duration closeTaskRetryInterval = Default.CLOSE_TASK_RETRY_INTERVAL;

	/**
	 * The timeout for the scheduler termination when closing the provider.
	 */
	private Duration schedulerTerminationTimeout = Default.SCHEDULER_TERMINATION_TIMEOUT;

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
	 * Returns the maximum refresh interval.
	 *
	 * @return the maximum refresh interval
	 */
	public Duration getMaxRefreshInterval() {
		return maxRefreshInterval;
	}

	/**
	 * Sets the maximum refresh interval.
	 *
	 * @param maxRefreshInterval the maximum refresh interval
	 */
	public void setMaxRefreshInterval(final Duration maxRefreshInterval) {
		this.maxRefreshInterval = maxRefreshInterval;
	}

	/**
	 * Returns the refresh failure delay multiplier.
	 *
	 * @return the refresh failure delay multiplier
	 */
	public Double getRefreshFailureDelayMultiplier() {
		return refreshFailureDelayMultiplier;
	}

	/**
	 * Sets the refresh failure delay multiplier.
	 *
	 * @param refreshFailureDelayMultiplier the refresh failure delay multiplier
	 */
	public void setRefreshFailureDelayMultiplier(final Double refreshFailureDelayMultiplier) {
		this.refreshFailureDelayMultiplier = refreshFailureDelayMultiplier;
	}

	/**
	 * Returns the maximum close attempts.
	 *
	 * @return the maximum close attempts
	 */
	public Integer getMaxTaskCloseAttempts() {
		return maxTaskCloseAttempts;
	}

	/**
	 * Sets the maximum close attempts.
	 *
	 * @param maxCloseAttempts the maximum close attempts to set
	 */
	public void setMaxTaskCloseAttempts(final Integer maxCloseAttempts) {
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

	/**
	 * Returns the scheduler termination timeout.
	 *
	 * @return the scheduler termination timeout
	 */
	public Duration getSchedulerTerminationTimeout() {
		return schedulerTerminationTimeout;
	}

	/**
	 * Sets the scheduler termination timeout.
	 *
	 * @param schedulerTerminationTimeout the scheduler termination timeout to set
	 */
	public void setSchedulerTerminationTimeout(final Duration schedulerTerminationTimeout) {
		this.schedulerTerminationTimeout = schedulerTerminationTimeout;
	}
}
