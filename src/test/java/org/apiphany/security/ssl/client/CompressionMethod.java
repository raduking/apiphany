package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

public class CompressionMethod {

	private CompressionMethodType method;

	public CompressionMethod(final CompressionMethodType method) {
		this.method = method;
	}

	public CompressionMethod(final byte method) {
		this.method = CompressionMethodType.fromValue(method);
	}

	public static CompressionMethod from(final InputStream is) throws IOException {
		int method = is.read();
		return new CompressionMethod((byte) method);
	}

	public byte[] toByteArray() {
		return Bytes.from(method.value());
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public CompressionMethodType getMethod() {
		return method;
	}
}
