package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum HandshakeType implements Sizeable {

	CLIENT_HELLO((byte) 0x01, ClientHello::from),
	SERVER_HELLO((byte) 0x02, null),
	CERTIFICATE((byte) 0x0B, null),
	SERVER_KEY_EXCHANGE((byte) 0x0C, null),
	SERVER_HELLO_DONE((byte) 0x0E, null),
	CLIENT_KEY_EXCHANGE((byte) 0x10, null),
	FINISHED((byte) 0x14, null);

	public static final int BYTES = 1;

	private static final Map<Byte, HandshakeType> VALUE_MAP = Enums.buildNameMap(values(), HandshakeType::value);

	private final byte value;

	private final FromFunction<? extends TLSObject> fromFunction;

	HandshakeType(final byte value, FromFunction<? extends TLSObject> fromFunction) {
		this.value = value;
		this.fromFunction = fromFunction;
	}

	public byte value() {
		return value;
	}

	public static HandshakeType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	@Override
	public int size() {
		return BYTES;
	}

	public FromFunction<? extends TLSObject> handshake() {
		return fromFunction;
	}
}
