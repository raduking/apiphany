package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum CompressionMethodType {

	NO_COMPRESSION((byte) 0x00);

	public static final int BYTES = 1;

	private static final Map<Byte, CompressionMethodType> VALUE_MAP = Enums.buildNameMap(values(), CompressionMethodType::value);

	private byte value;

	CompressionMethodType(final byte value) {
		this.value = value;
	}

	public byte value() {
		return value;
	}

	public static CompressionMethodType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}
}
