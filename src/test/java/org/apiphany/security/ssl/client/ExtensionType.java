package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.morphix.lang.Enums;

public enum ExtensionType implements TLSObject {

	SERVER_NAME((short) 0x0000, ServerNames::from),
	STATUS_REQUEST((short) 0x0005, StatusRequest::from),
	SUPPORTED_GROUPS((short) 0x000A, SupportedGroups::from),
	EC_POINTS_FORMAT((short) 0x000B, ECPointFormats::from),
	SIGNATURE_ALGORITHMS((short) 0x000D, SignatureAlgorithms::from),
	RENEGOTIATION_INFO((short) 0xFF01, RenegotiationInfo::from),
	SCT((short) 0x0012, SignedCertificateTimestamp::from),
	SESSION_TICKET((short) 0x0023, SessionTicket::from),
	EMT((short) 0x0016, ExtendedMasterSecret::from),

	/**
	 * Used by OpenSSL, non standard.
	 */
	RENEGOTIATION_INFO_EXTENDED((short) 0x0017, RenegotiationInfoExtended::from);

	public static final int BYTES = 2;

	public interface FromFunction {
		TLSExtension from(InputStream is, ExtensionType type) throws IOException;
	}

	private static final Map<Short, ExtensionType> VALUE_MAP = Enums.buildNameMap(values(), ExtensionType::value);

	private final short value;

	private final FromFunction fromFunction;

	ExtensionType(final short value, final FromFunction fromFunction) {
		this.value = value;
		this.fromFunction = fromFunction;
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

	@Override
	public byte[] toByteArray() {
		return Int16.toByteArray(value);
	}

	public TLSExtension extensionFrom(final InputStream is) throws IOException {
		return fromFunction.from(is, this);
	}
}
