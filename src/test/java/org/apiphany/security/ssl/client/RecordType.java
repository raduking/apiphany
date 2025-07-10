package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum RecordType implements Sizeable {

	HANDSHAKE((byte) 0x16, HandshakeMessage::from),
	CHANGE_CIPHER_SPEC((byte) 0x14, ChangeCipherSpec::from);

	public static final int BYTES = 1;

	private static final Map<Byte, RecordType> VALUE_MAP = Enums.buildNameMap(values(), RecordType::value);

	private final byte value;

	private final FromFunction<? extends TLSObject> fromFunction;

	RecordType(final byte value, FromFunction<? extends TLSObject> fromFunction) {
		this.value = value;
		this.fromFunction = fromFunction;
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

	public FromFunction<? extends TLSObject> message() {
		return fromFunction;
	}
}
