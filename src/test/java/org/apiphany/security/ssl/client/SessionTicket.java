package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class SessionTicket implements Extension {

	private ExtensionType type;

	private Int16 length;

	public SessionTicket(final ExtensionType type, final Int16 length) {
		this.type = type;
		this.length = length;
	}

	public SessionTicket() {
		this(ExtensionType.SESSION_TICKET, new Int16((short) 0x0000));
	}

	public static SessionTicket from(final InputStream is) throws IOException {
		Int16 extensionType = Int16.from(is);
		ExtensionType type = ExtensionType.fromValue(extensionType.getValue());

		return from(is, type);
	}

	public static SessionTicket from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);

		return new SessionTicket(type, length);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.writeShort(type.value());
			dos.write(length.toByteArray());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return type.size() + length.size();
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}
}

