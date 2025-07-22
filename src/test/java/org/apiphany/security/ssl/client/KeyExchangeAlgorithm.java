package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum KeyExchangeAlgorithm {

	ECDHE,
	DHE,
	RSA,
	NONE; // For TLS 1.3 and future post-handshake schemes

	private static final Map<String, KeyExchangeAlgorithm> NAME_MAP = Enums.buildNameMap(values());

	public static KeyExchangeAlgorithm fromValue(final String value) {
		return Enums.fromString(value, NAME_MAP, values());
	}

}
