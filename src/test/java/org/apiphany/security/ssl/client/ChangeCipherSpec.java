package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSObject;

public class ChangeCipherSpec implements TLSObject {

	private final UInt8 payload;

	public ChangeCipherSpec(final UInt8 payload) {
		this.payload = payload;
	}

	public ChangeCipherSpec(final byte payload) {
		this(UInt8.of(payload));
	}

	public ChangeCipherSpec() {
		this((byte) 0x01);
	}

	public static ChangeCipherSpec from(final InputStream is) throws IOException {
		UInt8 payload = UInt8.from(is);

		return new ChangeCipherSpec(payload);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(payload.toByteArray());
		return buffer.array();
	}

	@Override
	public int sizeOf() {
		return payload.sizeOf();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public UInt8 getPayload() {
		return payload;
	}
}
