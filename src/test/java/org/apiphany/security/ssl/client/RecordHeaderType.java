package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum RecordHeaderType {

	HANDSHAKE_RECORD((byte) 0x16);

	private static final Map<Byte, RecordHeaderType> VALUE_MAP = Enums.buildNameMap(values(), RecordHeaderType::value);

	private byte value;

	RecordHeaderType(byte value) {
		this.value = value;
	}

	byte value() {
		return value;
	}

	public static RecordHeaderType fromValue(byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}
}
