package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class Signature implements TLSObject {

	private Int16 reserved;

	private Int16 length;

	private BinaryData value;

	public Signature(final Int16 reserved, final Int16 length, final BinaryData value) {
		this.reserved = reserved;
		this.length = length;
		this.value = value;
	}

	public Signature(final Int16 reserved, final Int16 length, final byte[] bytes) {
		this(reserved, length, new BinaryData(bytes));
	}

	public Signature(final short length, final byte[] bytes) {
		this(new Int16((short) 0x0000), new Int16(length), bytes);
	}

	public static Signature from(final InputStream is) throws IOException {
		Int16 reserved = Int16.from(is);
		Int16 length = Int16.from(is);
		BinaryData value = BinaryData.from(is, length.getValue());

		return new Signature(reserved, length, value);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(reserved.toByteArray());
			dos.write(length.toByteArray());
			dos.write(value.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return reserved.size() + length.size() + value.size();
	}

	public Int16 getReserved() {
		return reserved;
	}

	public Int16 getLength() {
		return length;
	}

	public BinaryData getValue() {
		return value;
	}
}
