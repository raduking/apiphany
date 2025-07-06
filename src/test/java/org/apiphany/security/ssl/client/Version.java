package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

import com.fasterxml.jackson.annotation.JsonValue;

public class Version implements Sizeable {

	public static final int BYTES = 2;

	private SSLProtocol protocol;

	public Version(final SSLProtocol protocol) {
		this.protocol = protocol;
	}

	public static Version of(final SSLProtocol protocol) {
		return new Version(protocol);
	}

	public static Version from(final InputStream is) throws IOException {
		byte[] shortBuffer = new byte[Int16.BYTES];
		int bytesRead = is.read(shortBuffer);
		if (Int16.BYTES != bytesRead) {
			throw new EOFException("Short version, cannot read TLS version");
		}
		SSLProtocol protocol = SSLProtocol.fromVersion(Bytes.toShort(shortBuffer));

		return Version.of(protocol);
	}

	public byte[] toByteArray() {
		return Bytes.from(protocol.handshakeVersion());
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
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
