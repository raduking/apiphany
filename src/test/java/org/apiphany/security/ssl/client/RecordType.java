package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum RecordType implements Sizeable {

	CHANGE_CIPHER_SPEC((byte) 0x14, ChangeCipherSpec::from),
	ALERT((byte) 0x15, null),
	HANDSHAKE((byte) 0x16, TLSHandshake::from),
	APPLICATION_DATA((byte) 0x17, null),
	HEARTBEAT((byte) 0x18, null);

	public static final int BYTES = 1;

	private static final Map<Byte, RecordType> VALUE_MAP = Enums.buildNameMap(values(), RecordType::value);

	private final byte value;

	private final FromFunction.NoSize<? extends TLSObject> fromFunction;

	RecordType(final byte value, FromFunction.NoSize<? extends TLSObject> fromFunction) {
		this.value = value;
		this.fromFunction = fromFunction;
	}

	public byte value() {
		return value;
	}

	public static RecordType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	public static RecordType from(TLSObject tlsObject) {
		return switch(tlsObject) {
			case TLSHandshakeBody tlsHandshakeObject -> HANDSHAKE;
			case ChangeCipherSpec changeCipherSpec -> CHANGE_CIPHER_SPEC;
			default -> throw new UnsupportedOperationException("Unknown TLS object type: " + tlsObject.getClass());
		};
	}

	@Override
	public int size() {
		return BYTES;
	}

	public FromFunction.NoSize<? extends TLSObject> fragment() {
		return fromFunction;
	}
}
