package org.apiphany.auth.oauth2.client;

import java.util.Map;

import org.apiphany.lang.Strings;
import org.morphix.lang.Enums;

/**
 * OAuth2 form element names.
 *
 * @author Radu Sebastian LAZIN
 */
public enum OAuth2Form {

	CLIENT_SECRET("client_secret"),
	CLIENT_ID("client_id"),
	EXPIRES_IN("expires_in"),
	GRANT_TYPE("grant_type");

	/**
	 * Mapping of string values to enum constants for efficient lookup.
	 */
	private static final Map<String, OAuth2Form> NAME_MAP = Enums.buildNameMap(values());

	private final String value;

	OAuth2Form(final String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return value();
	}

	public static OAuth2Form fromString(final String method) {
		return Enums.fromString(Strings.safe(method).toLowerCase(), NAME_MAP, values());
	}
}
