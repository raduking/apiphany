package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apiphany.json.JsonBuilder;

public class ServerName implements Sizeable {

	private Int16 size;

	private Int8 type;

	private Int16 length;

	private BinaryData name;

	public ServerName(final Int16 size, final Int8 type, final Int16 length, final BinaryData name) {
		this.size = size;
		this.type = type;
		this.length = length;
		this.name = name;
	}

	public ServerName(final String name) {
		this(
				new Int16((short) (Int8.BYTES + Int16.BYTES + name.length())),
				new Int8((byte) 0x00),
				new Int16((short) name.length()),
				new BinaryData(name.getBytes(StandardCharsets.US_ASCII))
		);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.write(size.toByteArray());
		dos.write(type.toByteArray());
		dos.write(length.toByteArray());
		dos.write(name.toByteArray());

		return bos.toByteArray();
	}

	public static ServerName from(final InputStream is) throws IOException {
		Int16 size = Int16.from(is);
		Int8 type = Int8.from(is);
		Int16 length = Int16.from(is);
		BinaryData name = BinaryData.from(is, length.getValue());

		return new ServerName(size, type, length, name);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return size.size() + type.size() + length.size() + name.size();
	}

	public Int16 getSize() {
		return size;
	}

	public Int8 getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public BinaryData getName() {
		return name;
	}

	public String getNameASCII() {
		return new String(name.getBytes(), StandardCharsets.US_ASCII);
	}
}
