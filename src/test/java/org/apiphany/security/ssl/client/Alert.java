package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class Alert implements TLSObject {

	public static final int BYTES = 2;

	private AlertLevel level;

	private AlertDescription description;

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
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(level.value());
			dos.write(description.code());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public int size() {
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
