package org.apiphany.security.ssl.client;

import java.util.Map;

import org.apiphany.io.Int8;
import org.apiphany.security.tls.TLSObject;
import org.morphix.lang.Enums;

public enum AlertDescription implements TLSObject {

    CLOSE_NOTIFY((byte) 0x00, "Graceful connection closure"),
    UNEXPECTED_MESSAGE((byte) 0x0A, "Invalid message received"),
    BAD_RECORD_MAC((byte) 0x14, "MAC verification failed"),
    DECRYPTION_FAILED((byte) 0x15, "Decryption error (TLS 1.0) / Generic cipher mismatch (TLS 1.2+)"),
    RECORD_OVERFLOW((byte) 0x16, "TLS record exceeded max size"),
    HANDSHAKE_FAILURE((byte) 0x28, "No common ciphersuite or unsupported parameters"),
    ACCESS_DENIED((byte) 0x2F, "Certificate valid but access denied"),
    DECODE_ERROR((byte) 0x30, "Message corrupted (e.g., malformed certificate)"),
    UNSUPPORTED_EXTENSION((byte) 0x3C, "Peer doesn’t support the extension"),
    PROTOCOL_VERSION((byte) 0x46, "Unsupported TLS version"),
    INTERNAL_ERROR((byte) 0x50, "Unspecified implementation error");

	public static final int BYTES = 1;

	private static final Map<Byte, AlertDescription> VALUE_MAP = Enums.buildNameMap(values(), AlertDescription::code);

	private final byte code;
	private final String description;

	AlertDescription(final byte code, final String description) {
		this.code = code;
		this.description = description;
	}

	public byte code() {
		return code;
	}

	public String description() {
		return description;
	}

	@Override
	public String toString() {
		return name().toLowerCase() + ": " + description();
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public byte[] toByteArray() {
		return Int8.toByteArray(code);
	}

	public static AlertDescription fromCode(final byte value) {
		return Enums.from(value, VALUE_MAP, values());
	}
}
