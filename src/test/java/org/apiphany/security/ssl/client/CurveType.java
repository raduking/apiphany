package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum CurveType implements TLSObject {

	NAMED_CURVE((byte) 0x03);

	public static final int BYTES = 1;

	private static final Map<Byte, CurveType> VALUE_MAP = Enums.buildNameMap(values(), CurveType::value);

	private final byte value;

	CurveType(final byte value) {
		this.value = value;
	}

	public byte value() {
		return value;
	}

	public static CurveType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	@Override
	public int size() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return Int8.toByteArray(value);
	}
}
