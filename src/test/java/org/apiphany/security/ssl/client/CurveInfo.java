package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class CurveInfo implements TLSObject {

	private CurveType type;

	private CurveName name;

	public CurveInfo(final CurveType type, final CurveName name) {
		this.type = type;
		this.name = name;
	}

	public static CurveInfo from(final InputStream is) throws IOException {
		Int8 typeValue = Int8.from(is);
		CurveType type = CurveType.fromValue(typeValue.getValue());

		Int16 nameValue = Int16.from(is);
		CurveName name = CurveName.fromValue(nameValue.getValue());

		return new CurveInfo(type, name);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.writeByte(type.value());
			dos.writeShort(name.value());
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return CurveType.BYTES + CurveName.BYTES;
	}

	public CurveType getType() {
		return type;
	}

	public CurveName getName() {
		return name;
	}
}
