package org.apiphany.security.ssl.client;

import java.util.Map;

import org.apiphany.io.Int8;
import org.apiphany.security.tls.TLSObject;
import org.morphix.lang.Enums;

public enum RecordContentType implements TLSObject {

	CHANGE_CIPHER_SPEC((byte) 0x14, FromFunction.ignoreSize(ChangeCipherSpec::from)),
	ALERT((byte) 0x15, FromFunction.ignoreSize(Alert::from)),
	HANDSHAKE((byte) 0x16, FromFunction.ignoreSize(Handshake::from)),
	APPLICATION_DATA((byte) 0x17, ApplicationData::from),
	HEARTBEAT((byte) 0x18, null);

	public static final int BYTES = 1;

	private static final Map<Byte, RecordContentType> VALUE_MAP = Enums.buildNameMap(values(), RecordContentType::value);

	private final byte value;

	private final FromFunction<? extends TLSObject> fromFunction;

	RecordContentType(final byte value, final FromFunction<? extends TLSObject> fromFunction) {
		this.value = value;
		this.fromFunction = fromFunction;
	}

	public byte value() {
		return value;
	}

	public static RecordContentType fromValue(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	public static RecordContentType from(final TLSObject tlsObject) {
		return switch(tlsObject) {
			case TLSHandshakeBody tlsHandshakeBody -> HANDSHAKE;
			case ChangeCipherSpec changeCipherSpec -> CHANGE_CIPHER_SPEC;
			case Encrypted encrypted -> HANDSHAKE;
			case ApplicationData applicationData -> APPLICATION_DATA;
			default -> throw new UnsupportedOperationException("Unknown TLS object type: " + tlsObject.getClass());
		};
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return Int8.toByteArray(value);
	}

	public FromFunction<? extends TLSObject> fragment() {
		return fromFunction;
	}
}
