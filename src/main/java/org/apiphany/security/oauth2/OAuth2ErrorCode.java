package org.apiphany.security.oauth2;

import java.util.Map;

import org.apiphany.lang.annotation.AsValue;
import org.apiphany.lang.annotation.Creator;
import org.morphix.lang.Enums;

/**
 * Standard OAuth 2.0 error codes as defined in RFC 6749 §5.2.
 * <p>
 * Additional or unknown values are mapped to {@link #UNKNOWN}.
 * </p>
 *
 * @author Radu Sebastian LAZIN
 */
public enum OAuth2ErrorCode {

	/**
	 * The request is missing a required parameter or is malformed.
	 */
	INVALID_REQUEST("invalid_request"),

	/**
	 * Client authentication failed (unknown client, bad credentials, etc.).
	 */
	INVALID_CLIENT("invalid_client"),

	/**
	 * The provided authorization grant or refresh token is invalid or expired.
	 */
	INVALID_GRANT("invalid_grant"),

	/**
	 * The authenticated client is not authorized to use this grant type.
	 */
	UNAUTHORIZED_CLIENT("unauthorized_client"),

	/**
	 * The authorization grant type is not supported by the server.
	 */
	UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),

	/**
	 * The requested scope is invalid, unknown, or exceeds what the client may request.
	 */
	INVALID_SCOPE("invalid_scope"),

	/**
	 * Non-standard or unrecognized error code.
	 */
	UNKNOWN("unknown");

	/**
	 * The name map for easy from string implementation.
	 */
	private static final Map<String, OAuth2ErrorCode> NAME_MAP = Enums.buildNameMap(values());

	/**
	 * The string value.
	 */
	private final String value;

	/**
	 * Constructor with the value.
	 *
	 * @param value the string value
	 */
	OAuth2ErrorCode(final String value) {
		this.value = value;
	}

	/**
	 * Returns the string value of the error code.
	 *
	 * @return the string representation used in JSON
	 */
	@AsValue
	public String getValue() {
		return value;
	}

	/**
	 * Resolves an {@link OAuth2ErrorCode} from its string value.
	 *
	 * @param value the string error code
	 * @return the matching enum constant, or {@link #UNKNOWN} if none matches
	 */
	@Creator
	public static OAuth2ErrorCode fromString(final String value) {
		return Enums.fromString(value, NAME_MAP, () -> UNKNOWN);
	}

	/**
	 * @see #toString()
	 */
	@Override
	public String toString() {
		return value;
	}
}
