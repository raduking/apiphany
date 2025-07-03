package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class CurveInfo {

	private CurveType type;

	private CurveName name;

	public CurveInfo(final CurveType type, final CurveName name) {
		this.type = type;
		this.name = name;
	}

	public static CurveInfo from(final InputStream is) throws IOException {
		int typeValue = is.read();
		CurveType type = CurveType.fromValue((byte) typeValue);

		Int16 nameValue = Int16.from(is);
		CurveName name = CurveName.fromValue(nameValue.getValue());

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
