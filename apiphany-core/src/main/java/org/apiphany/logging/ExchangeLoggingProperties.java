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
	 * Extra header names to redact (case-insensitive).
	 */
	private List<String> sensitiveHeaders;

	/**
	 * Extra parameter names to redact (case-insensitive).
	 */
	private List<String> sensitiveParams;

	/**
	 * Body logging mode. Defaults to full body logging for backward compatibility.
	 */
	private Logging.Mode bodyLoggingMode = Logging.Mode.FULL;

	/**
	 * Default constructor.
	 */
	public ExchangeLoggingProperties() {
		// empty
	}

	/**
	 * Returns extra sensitive headers.
	 *
	 * @return extra sensitive headers
	 */
	public List<String> getSensitiveHeaders() {
		return sensitiveHeaders;
	}

	/**
	 * Sets extra sensitive headers.
	 *
	 * @param sensitiveHeaders extra sensitive headers
	 */
	public void setSensitiveHeaders(final List<String> sensitiveHeaders) {
		this.sensitiveHeaders = sensitiveHeaders;
	}

	/**
	 * Returns extra sensitive parameters.
	 *
	 * @return extra sensitive parameters
	 */
	public List<String> getSensitiveParams() {
		return sensitiveParams;
	}

	/**
	 * Sets extra sensitive parameters.
	 *
	 * @param sensitiveParams extra sensitive parameters
	 */
	public void setSensitiveParams(final List<String> sensitiveParams) {
		this.sensitiveParams = sensitiveParams;
	}

	/**
	 * Returns body logging mode.
	 *
	 * @return body logging mode
	 */
	public Logging.Mode getBodyLoggingMode() {
		return bodyLoggingMode;
	}

	/**
	 * Sets body logging mode.
	 *
	 * @param bodyLoggingMode body logging mode
	 */
	public void setBodyLoggingMode(final Logging.Mode bodyLoggingMode) {
		this.bodyLoggingMode = bodyLoggingMode;
	}

	/**
	 * Returns true if the provided header name exists in {@link #sensitiveHeaders}.
	 *
	 * @param headerName header name
	 * @return true if configured as sensitive
	 */
	public boolean containsSensitiveHeader(final String headerName) {
		return Strings.containsIgnoreCase(headerName, sensitiveHeaders);
	}

	/**
	 * Returns true if the provided parameter name exists in {@link #sensitiveParams}.
	 *
	 * @param parameterName parameter name
	 * @return true if configured as sensitive
	 */
	public boolean containsSensitiveParam(final String parameterName) {
		return Strings.containsIgnoreCase(parameterName, sensitiveParams);
	}
}
