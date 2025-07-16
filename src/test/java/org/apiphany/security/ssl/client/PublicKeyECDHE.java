package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;

import org.apiphany.json.JsonBuilder;

public class PublicKeyECDHE implements TLSObject {

	private Int8 length;

	private BinaryData value;

	public PublicKeyECDHE(final Int8 length, final BinaryData value) {
		this.length = length;
		this.value = value;
	}

	public PublicKeyECDHE(final Int8 length, final byte[] bytes) {
		this(length, new BinaryData(bytes));
	}

	public PublicKeyECDHE(final byte length, final byte[] bytes) {
		this(new Int8(length), bytes);
	}

	public PublicKeyECDHE(final byte[] bytes) {
		this((byte) bytes.length, bytes);
	}

	public static PublicKeyECDHE from(final InputStream is) throws IOException {
		Int8 length = Int8.from(is);
		BinaryData value = BinaryData.from(is, length.getValue());

		return new PublicKeyECDHE(length, value);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		buffer.put(value.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public Int8 getLength() {
		return length;
	}

	public BinaryData getValue() {
		return value;
	}

	@Override
	public int sizeOf() {
		return length.sizeOf() + value.sizeOf();
	}

	public PublicKey loadX25519PublicKey() throws Exception {
	    KeyFactory kf = KeyFactory.getInstance("X25519");
	    NamedParameterSpec spec = new NamedParameterSpec("X25519");
	    XECPublicKeySpec pubSpec = new XECPublicKeySpec(spec, new BigInteger(1, value.getBytes()));
	    return kf.generatePublic(pubSpec);
	}
}
