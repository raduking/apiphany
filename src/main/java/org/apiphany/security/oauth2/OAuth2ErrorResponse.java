package org.apiphany.security.oauth2;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.annotation.FieldName;

/**
 * Represents an OAuth 2.0 error response as defined in
 * <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-5.2">RFC 6749, Section 5.2</a>.
 *
 * <p>
 * This class models the standard fields returned by an authorization server when a token request fails, as well as
 * commonly used extensions such as {@code error_uri} or vendor-specific fields.
 * </p>
 *
 * <p>
 * Example JSON response:
 * </p>
 *
 * <pre>
 * {
 *   "error": "invalid_client",
 *   "error_description": "Client authentication failed",
 *   "error_uri": "https://server.example.com/errors/invalid_client"
 * }
 * </pre>
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-5.2">RFC 6749 §5.2</a>
 *
 * @author Radu Sebastian LAZIN
 */
public class OAuth2ErrorResponse {

	/**
	 * A single ASCII error code identifying the type of error that occurred.
	 * <p>
	 * Required field.
	 * </p>
	 * <p>
	 * Example: {@code "invalid_client"}, {@code "unauthorized_client"}.
	 * </p>
	 */
	@FieldName("error")
	private OAuth2ErrorCode error;

	/**
	 * A human-readable ASCII text providing additional information about the error, intended for a developer or
	 * administrator.
	 * <p>
	 * Optional field.
	 * </p>
	 */
	@FieldName("error_description")
	private String errorDescription;

	/**
	 * A URI identifying a human-readable web page with information about the error.
	 * <p>
	 * Optional field.
	 * </p>
	 */
	@FieldName("error_uri")
	private String errorUri;

	/**
	 * The state parameter sent in the request, if present. Some servers include it back in the error response to help the
	 * client maintain context.
	 * <p>
	 * Optional field.
	 * </p>
	 */
	@FieldName("state")
	private String state;

	/**
	 * Non-standard field occasionally returned by servers such as Keycloak or OAuth2 libraries to provide more specific
	 * debugging hints.
	 * <p>
	 * Optional, non-standard.
	 * </p>
	 */
	@FieldName("hint")
	private String hint;

	/**
	 * Default constructor.
	 */
	public OAuth2ErrorResponse() {
		// empty
	}

	/**
	 * Returns the error code.
	 *
	 * @return the error code (e.g., {@code "invalid_client"})
	 */
	public OAuth2ErrorCode getError() {
		return error;
	}

	/**
	 * Sets the error code.
	 *
	 * @param error the error code to set
	 */
	public void setError(final OAuth2ErrorCode error) {
		this.error = error;
	}

	/**
	 * Returns the human-readable error description.
	 *
	 * @return the error description, or {@code null} if not provided
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * Sets the human-readable error description.
	 *
	 * @param errorDescription the description to set
	 */
	public void setErrorDescription(final String errorDescription) {
		this.errorDescription = errorDescription;
	}

	/**
	 * Returns the URI with additional information about the error.
	 *
	 * @return the error URI, or {@code null} if not provided
	 */
	public String getErrorUri() {
		return errorUri;
	}

	/**
	 * Sets the URI with additional information about the error.
	 *
	 * @param errorUri the URI to set
	 */
	public void setErrorUri(final String errorUri) {
		this.errorUri = errorUri;
	}

	/**
	 * Returns the state parameter value, if included in the response.
	 *
	 * @return the state, or {@code null} if not provided
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets the state parameter value.
	 *
	 * @param state the state to set
	 */
	public void setState(final String state) {
		this.state = state;
	}

	/**
	 * Returns the non-standard hint field, if provided.
	 *
	 * @return the hint, or {@code null} if not present
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * Sets the non-standard hint field.
	 *
	 * @param hint the hint to set
	 */
	public void setHint(final String hint) {
		this.hint = hint;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}
}
