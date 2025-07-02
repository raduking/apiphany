package org.apiphany.security.ssl.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class CurveInfo {

	private CurveType type;

	private CurveName name;

	public CurveInfo(CurveType type, CurveName name) {
		this.type = type;
		this.name = name;
	}

	public static CurveInfo from(InputStream is) throws IOException {
		int typeValue = is.read();
		CurveType type = CurveType.fromValue((byte) typeValue);

		byte[] shortBuffer = new byte[Bytes.Size.SHORT];
		int bytesRead = is.read(shortBuffer);
		if (Bytes.Size.SHORT != bytesRead) {
			throw new EOFException("Short curve info, cannot read curve name");
		}
		short nameValue = Bytes.toShort(shortBuffer);
		CurveName name = CurveName.fromValue(nameValue);

		return new CurveInfo(type, name);
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public CurveType getType() {
		return type;
	}

	public CurveName getName() {
		return name;
	}
}
