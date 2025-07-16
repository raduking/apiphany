package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum AlertLevel implements TLSObject {

	WARNING((byte) 0x01),
	FATAL((byte) 0x02);

	public static final int BYTES = 1;

	private static final Map<Byte, AlertLevel> VALUE_MAP = Enums.buildNameMap(values(), AlertLevel::value);

	private final byte value;

	AlertLevel(final byte value) {
		this.value = value;
	}

	public byte value() {
		return value;
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return Int8.toByteArray(value);
	}

	public static AlertLevel fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}
}
