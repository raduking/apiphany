package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum RecordType implements Sizeable {

	HANDSHAKE((byte) 0x16),
	CHANGE_CIPHER_SPEC((byte) 0x14);

	public static final int BYTES = 1;

	private static final Map<Byte, RecordType> VALUE_MAP = Enums.buildNameMap(values(), RecordType::value);

	private byte value;

	RecordType(final byte value) {
		this.value = value;
	}

	public byte value() {
		return value;
	}

	public static RecordType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	@Override
	public int size() {
		return BYTES;
	}
}
