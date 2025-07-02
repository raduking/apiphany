package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum CurveName {

	X25519((short) 0x001D),
	SECP256R1((short) 0x0017),
	SECP384R1((short) 0x0018),
	SECP521R1((short) 0x0019);

	private static final Map<Short, CurveName> VALUE_MAP = Enums.buildNameMap(values(), CurveName::value);

	private short value;

	CurveName(short value) {
		this.value = value;
	}

	public static CurveName fromValue(short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	public short value() {
		return value;
	}
}
