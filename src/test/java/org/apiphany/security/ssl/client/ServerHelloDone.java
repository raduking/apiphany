package org.apiphany.security.ssl.client;

import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.Bytes;

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
		return Bytes.EMPTY;
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public HandshakeType getType() {
		return HandshakeType.SERVER_HELLO_DONE;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

}
