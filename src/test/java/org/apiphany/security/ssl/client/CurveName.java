package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum CurveName implements TLSObject {

	X25519((short) 0x001D),
	SECP256R1((short) 0x0017),
	SECP384R1((short) 0x0018),
	SECP521R1((short) 0x0019),
	X448((short) 0x001E),
	SECP256K1((short) 0x0023);

	public static final int BYTES = 2;

	private static final Map<Short, CurveName> VALUE_MAP = Enums.buildNameMap(values(), CurveName::value);

	private final short value;

	CurveName(final short value) {
		this.value = value;
	}

	public static CurveName fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	public short value() {
		return value;
	}

	@Override
	public int size() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return Int16.toByteArray(value);
	}
}
