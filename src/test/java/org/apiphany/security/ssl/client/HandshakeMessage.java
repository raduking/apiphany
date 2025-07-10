package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class HandshakeMessage implements TLSObject {

	private HandshakeHeader header;

	private TLSObject body;

	public HandshakeMessage(HandshakeHeader header, TLSObject body) {
		this.header = header;
		this.body = body;
	}

	public static HandshakeMessage from(InputStream is) throws IOException {
		HandshakeHeader header = HandshakeHeader.from(is);
		HandshakeType type = header.getType();

		TLSObject body = type.handshake().from(is);

		return new HandshakeMessage(header, body);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(header.toByteArray());
			dos.write(body.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return header.size() + body.size();
	}

	public HandshakeHeader getHeader() {
		return header;
	}

	public Sizeable getBody() {
		return body;
	}
}
