package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum RecordHeaderType {

	HANDSHAKE_RECORD((byte) 0x16),
	CHANGE_CIPHER_SPEC((byte) 0x14);

	private static final Map<Byte, RecordHeaderType> VALUE_MAP = Enums.buildNameMap(values(), RecordHeaderType::value);

	private byte value;

	RecordHeaderType(final byte value) {
		this.value = value;
	}

	byte value() {
		return value;
	}

	public static RecordHeaderType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}
}
