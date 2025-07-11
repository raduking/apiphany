package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.XECPublicKeySpec;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(length.toByteArray());
			dos.write(value.toByteArray());
		}).run();
		return bos.toByteArray();
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
	public int size() {
		return length.size() + value.size();
	}

	public PublicKey loadX25519PublicKey() throws Exception {
	    KeyFactory kf = KeyFactory.getInstance("X25519");
	    NamedParameterSpec spec = new NamedParameterSpec("X25519");
	    XECPublicKeySpec pubSpec = new XECPublicKeySpec(spec, new BigInteger(1, value.getBytes()));
	    return kf.generatePublic(pubSpec);
	}
}
