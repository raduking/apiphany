package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum CurveType {

	NAMED_CURVE((byte) 0x03);

	private static final Map<Byte, CurveType> VALUE_MAP = Enums.buildNameMap(values(), CurveType::value);

	private byte value;

	CurveType(byte value) {
		this.value = value;
	}

	byte value() {
		return value;
	}

	public static CurveType fromValue(byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}
}
