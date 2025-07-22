package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt24;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSObject;
import org.morphix.lang.JavaObjects;

public class Handshake implements TLSObject {

	private final HandshakeHeader header;

	private final TLSHandshakeBody body;

	public Handshake(final HandshakeHeader header, final TLSHandshakeBody body, final boolean updateHeader) {
		this.header = updateHeader ? new HandshakeHeader(header.getType(), UInt24.of(body.sizeOf())) : header;
		this.body = body;
	}

	public Handshake(final TLSHandshakeBody body) {
		this(new HandshakeHeader(body.getType()), body, true);
	}

	public Handshake(final HandshakeHeader header, final TLSHandshakeBody body) {
		this(header, body, true);
	}

	public static Handshake from(final InputStream is) throws IOException {
		HandshakeHeader header = HandshakeHeader.from(is);
		HandshakeType type = header.getType();

		TLSHandshakeBody body = type.handshake().from(is, header.getLength().getValue());

		return new Handshake(header, body, false);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(header.toByteArray());
		buffer.put(body.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return header.sizeOf() + body.sizeOf();
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
