package org.apiphany.security.ssl.client;

import java.util.Map;

import org.morphix.lang.Enums;

public enum ExtensionType implements Sizeable {

	SERVER_NAME((short) 0x0000),
	STATUS_REQUEST((short) 0x0005),
	SUPPORTED_GROUPS((short) 0x000A),
	EC_POINTS_FORMAT((short) 0x000B),
	SIGNATURE_ALGORITHMS((short) 0x000D),
	RENEGOTIATION_INFO((short) 0xFF01),
	SCT((short) 0x0012);

	public static final int BYTES = 2;

	private static final Map<Short, ExtensionType> VALUE_MAP = Enums.buildNameMap(values(), ExtensionType::value);

	private short value;

	ExtensionType(final short value) {
		this.value = value;
	}

	public static ExtensionType fromValue(final short value) {
		return Enums.from(value, VALUE_MAP, values());
	}

	public short value() {
		return value;
	}

	@Override
	public int size() {
		return BYTES;
	}
}
