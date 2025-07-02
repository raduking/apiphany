package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum HandshakeMessageType {

	CLIENT_HELLO((byte) 0x01),
	SERVER_HELLO((byte) 0x02),
	CERTIFICATE((byte) 0x0B),
	SERVER_KEY_EXCHANGE((byte) 0x0C),
	SERVER_HELLO_DONE((byte) 0x0E),
	CLIENT_KEY_EXCHANGE((byte) 0x10);

	private static final Map<Byte, HandshakeMessageType> VALUE_MAP = Enums.buildNameMap(values(), HandshakeMessageType::value);

	private byte value;

	HandshakeMessageType(byte value) {
		this.value = value;
	}

	byte value() {
		return value;
	}

	public static HandshakeMessageType fromValue(byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}
}
