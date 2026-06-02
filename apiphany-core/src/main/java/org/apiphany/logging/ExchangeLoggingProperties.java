package org.apiphany.logging;

import java.util.List;

import org.apiphany.lang.Strings;

/**
 * Configurable logging properties used by exchange clients and {@link ExchangeLogger}.
 *
 * @author Radu Sebastian LAZIN
 */
public class ExchangeLoggingProperties {

	/**
	 * Root custom-properties key under {@code ClientProperties.custom}.
	 */
	public static final String ROOT = "logging";

	/**
	 * Header logging configuration.
	 */
	private Category headers = new Category();

	/**
	 * Request parameter logging configuration.
	 */
	private Category params = new Category();

	/**
	 * Body logging configuration.
	 */
	private Category body = new Category();

	/**
	 * Default constructor.
	 */
	public ExchangeLoggingProperties() {
		// empty
	}

	/**
	 * Returns header logging configuration.
	 *
	 * @return header logging configuration
	 */
	public Category getHeaders() {
		return headers;
	}

	/**
	 * Sets header logging configuration.
	 *
	 * @param headers header logging configuration
	 */
	public void setHeaders(final Category headers) {
		this.headers = headers;
	}

	/**
	 * Returns request parameter logging configuration.
	 *
	 * @return request parameter logging configuration
	 */
	public Category getParams() {
		return params;
	}

	/**
	 * Sets request parameter logging configuration.
	 *
	 * @param params request parameter logging configuration
	 */
	public void setParams(final Category params) {
		this.params = params;
	}

	/**
	 * Returns body logging configuration.
	 *
	 * @return body logging configuration
	 */
	public Category getBody() {
		return body;
	}

	/**
	 * Sets body logging configuration.
	 *
	 * @param body body logging configuration
	 */
	public void setBody(final Category body) {
		this.body = body;
	}

	/**
	 * Returns true if the provided header name exists in {@link Category#getSensitive()} of {@link #getHeaders()}.
	 *
	 * @param headerName header name
	 * @return true if configured as sensitive
	 */
	public boolean containsSensitiveHeader(final String headerName) {
		return Strings.containsIgnoreCase(headerName, getHeaders().getSensitive());
	}

	/**
	 * Returns true if the provided parameter name exists in {@link Category#getSensitive()} of {@link #getParams()}.
	 *
	 * @param parameterName parameter name
	 * @return true if configured as sensitive
	 */
	public boolean containsSensitiveParam(final String parameterName) {
		return Strings.containsIgnoreCase(parameterName, getParams().getSensitive());
	}

	/**
	 * Generic logging category configuration, used for headers, parameters and body.
	 *
	 * @author Radu Sebastian LAZIN
	 */
	public static class Category {

		/**
		 * Logging mode for this category.
		 */
		private Logging.Mode mode = Logging.Mode.FULL;

		/**
		 * Extra values to redact (case-insensitive).
		 */
		private List<String> sensitive;

		/**
		 * Returns logging mode for this category.
		 *
		 * @return logging mode
		 */
		public Logging.Mode getMode() {
			return mode;
		}

		/**
		 * Sets logging mode for this category.
		 *
		 * @param mode logging mode
		 */
		public void setMode(final Logging.Mode mode) {
			this.mode = mode;
		}

		/**
		 * Returns the case-insensitive list of values considered sensitive for this category.
		 *
		 * @return sensitive values for this category
		 */
		public List<String> getSensitive() {
			return sensitive;
		}

		/**
		 * Sets the case-insensitive list of values considered sensitive for this category.
		 *
		 * @param sensitive sensitive values for this category
		 */
		public void setSensitive(final List<String> sensitive) {
			this.sensitive = sensitive;
		}
	}
}
