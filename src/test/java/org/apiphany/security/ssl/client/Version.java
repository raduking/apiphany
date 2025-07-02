package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.security.ssl.SSLProtocol;

public class Version {

	private SSLProtocol protocol;

	public Version(SSLProtocol protocol) {
		this.protocol = protocol;
	}

	public static Version of(SSLProtocol protocol) {
		return new Version(protocol);
	}

	public static Version from(InputStream is) throws IOException {
		byte[] shortBuffer = new byte[Bytes.Size.SHORT];
		int bytesRead = is.read(shortBuffer);
		if (Bytes.Size.SHORT != bytesRead) {
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

	public SSLProtocol getProtocol() {
		return protocol;
	}
}
