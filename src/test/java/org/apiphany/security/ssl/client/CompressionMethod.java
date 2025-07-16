package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum CompressionMethod implements TLSObject {

	NO_COMPRESSION((byte) 0x00);

	public static final int BYTES = 1;

	private static final Map<Byte, CompressionMethod> VALUE_MAP = Enums.buildNameMap(values(), CompressionMethod::value);

	private final byte value;

	CompressionMethod(final byte value) {
		this.value = value;
	}

	public byte value() {
		return value;
	}

	public static CompressionMethod fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return Int8.toByteArray(value);
	}
}
