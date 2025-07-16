package org.apiphany.security.ssl.client;

import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class ServerHelloDone implements TLSHandshakeBody {

	public static final int BYTES = 0;

	public ServerHelloDone() {
		// empty
	}

	@SuppressWarnings("unused")
	public static ServerHelloDone from(final InputStream is) {
		return new ServerHelloDone();
	}

	@Override
	public byte[] toByteArray() {
		return BinaryData.EMPTY;
	}

	@Override
	public int size() {
		return BYTES;
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.SERVER_HELLO_DONE;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

}
