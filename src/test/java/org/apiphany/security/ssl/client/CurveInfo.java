package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apiphany.json.JsonBuilder;

public class CurveInfo implements TLSObject {

	private CurveType type;

	private CurveName name;

	public CurveInfo(final CurveType type, final CurveName name) {
		this.type = type;
		this.name = name;
	}

	public static CurveInfo from(final InputStream is) throws IOException {
		Int8 int8 = Int8.from(is);
		CurveType type = CurveType.fromValue(int8.getValue());

		Int16 int16 = Int16.from(is);
		CurveName name = CurveName.fromValue(int16.getValue());

		return new CurveInfo(type, name);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(name.toByteArray());
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf() + name.sizeOf();
	}

	public CurveType getType() {
		return type;
	}

	public CurveName getName() {
		return name;
	}
}
