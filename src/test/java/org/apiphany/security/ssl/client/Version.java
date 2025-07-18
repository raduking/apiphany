package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

import com.fasterxml.jackson.annotation.JsonValue;

public class Version implements TLSObject {

	public static final int BYTES = 2;

	private final SSLProtocol protocol;

	public Version(final SSLProtocol protocol) {
		this.protocol = protocol;
	}

	public static Version of(final SSLProtocol protocol) {
		return new Version(protocol);
	}

	public static Version from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		SSLProtocol protocol = SSLProtocol.fromVersion(int16.getValue());

		return Version.of(protocol);
	}

	@Override
	public byte[] toByteArray() {
		return Int16.toByteArray(protocol.handshakeVersion());
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}

	public SSLProtocol getProtocol() {
		return protocol;
	}

	@JsonValue
	public String getProtocolString() {
		return protocol.value();
	}
}
