package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

public class Handshake implements Sizeable {

	private HandshakeHeader header;

	public Handshake(HandshakeHeader header) {
		this.header = header;
	}

	public static Handshake from(InputStream is) throws IOException {
		HandshakeHeader header = HandshakeHeader.from(is);

		return new Handshake(header);
	}

	@Override
	public int size() {
		return header.size();
	}

	public HandshakeHeader getHeader() {
		return header;
	}
}
