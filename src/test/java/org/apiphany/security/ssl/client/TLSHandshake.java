package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.JavaObjects;
import org.morphix.lang.function.ThrowingRunnable;

public class TLSHandshake implements TLSObject {

	private HandshakeHeader header;

	private TLSHandshakeBody body;

	public TLSHandshake(final HandshakeHeader header, final TLSHandshakeBody body, final boolean updateHeader) {
		this.header = header;
		this.body = body;
		if (updateHeader) {
			this.header.getLength().setValue((short) (body.size()));
		}
	}

	public TLSHandshake(final TLSHandshakeBody body) {
		this(new HandshakeHeader(body.type()), body, true);
	}

	public TLSHandshake(final HandshakeHeader header, final TLSHandshakeBody body) {
		this(header, body, true);
	}

	public static TLSHandshake from(final InputStream is) throws IOException {
		HandshakeHeader header = HandshakeHeader.from(is);
		HandshakeType type = header.getType();

		TLSHandshakeBody body = type.handshake().from(is, header.getLength().getValue());

		return new TLSHandshake(header, body, false);
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

	public TLSHandshakeBody getBody() {
		return body;
	}

	public <T extends TLSHandshakeBody> boolean is(final Class<T> tlsObjectClass) {
		return body.getClass().isAssignableFrom(tlsObjectClass);
	}

	public <T extends TLSHandshakeBody> T get(final Class<T> tlsObjectClass) {
		if (is(tlsObjectClass)) {
			return JavaObjects.cast(body);
		}
		throw new IllegalArgumentException("Cannot cast TLS handshake body from " + body.getClass() + " to " + tlsObjectClass);
	}
}
