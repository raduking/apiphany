package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;

import org.apiphany.json.JsonBuilder;

import com.fasterxml.jackson.annotation.JsonValue;

public class CompressionMethod implements Sizeable {

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
		return Int8.toByteArray(method.value());
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return CompressionMethodType.BYTES;
	}

	@JsonValue
	public CompressionMethodType getMethod() {
		return method;
	}
}
