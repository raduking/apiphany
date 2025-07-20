package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.io.Int8;
import org.apiphany.json.JsonBuilder;
import org.apiphany.security.tls.TLSObject;

public class Alert implements TLSObject {

	public static final int BYTES = 2;

	private final AlertLevel level;

	private final AlertDescription description;

	public Alert(final AlertLevel level, final AlertDescription description) {
		this.level = level;
		this.description = description;
	}

	public Alert(final byte level, final byte code) {
		this(AlertLevel.fromValue(level), AlertDescription.fromCode(code));
	}

	public static Alert from(final InputStream is) throws IOException {
		Int8 int81 = Int8.from(is);
		Int8 int82 = Int8.from(is);

		return new Alert(int81.getValue(), int82.getValue());
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(level.toByteArray());
		buffer.put(description.toByteArray());
		return buffer.array();
	}

	@Override
	public int sizeOf() {
		return BYTES;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public AlertLevel getLevel() {
		return level;
	}

	public String getDescription() {
		return description.toString();
	}
}
