package org.apiphany.security.ssl.client;

import java.util.Map;

import org.apiphany.io.Int8;
import org.apiphany.security.tls.TLSObject;
import org.morphix.lang.Enums;

public enum HandshakeType implements TLSObject {

	CLIENT_HELLO((byte) 0x01, FromFunction.ignoreSize(ClientHello::from)),
	SERVER_HELLO((byte) 0x02, FromFunction.ignoreSize(ServerHello::from)),
	CERTIFICATE((byte) 0x0B, FromFunction.ignoreSize(Certificates::from)),
	SERVER_KEY_EXCHANGE((byte) 0x0C, FromFunction.ignoreSize(ServerKeyExchange::from)),
	SERVER_HELLO_DONE((byte) 0x0E, FromFunction.ignoreSize(ServerHelloDone::from)),
	CLIENT_KEY_EXCHANGE((byte) 0x10, ClientKeyExchange::from),
	FINISHED((byte) 0x14, Finished::from);

	public static final int BYTES = 1;

	private static final Map<Byte, HandshakeType> VALUE_MAP = Enums.buildNameMap(values(), HandshakeType::value);

	private final byte value;

	private final FromFunction<? extends TLSHandshakeBody> fromFunction;

	HandshakeType(final byte value, final FromFunction<? extends TLSHandshakeBody> fromFunction) {
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
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return Int8.toByteArray(value);
	}

	public FromFunction<? extends TLSHandshakeBody> handshake() {
		return fromFunction;
	}
}
