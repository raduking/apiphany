package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt8;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;
import org.apiphany.security.tls.TLSObject;

public class CompressionMethods implements TLSObject {

	private final UInt8 size;

	private final List<CompressionMethod> methods;

	public CompressionMethods(final UInt8 size, final List<CompressionMethod> methods) {
		this.size = size;
		this.methods = new ArrayList<>(this.size.getValue());
		this.methods.addAll(methods);
	}

	public CompressionMethods(final List<CompressionMethod> methods) {
		this(UInt8.of((byte) methods.size()), methods);
	}

	public CompressionMethods() {
		this(List.of(CompressionMethod.NO_COMPRESSION));
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(size.toByteArray());
		for (CompressionMethod method : methods) {
			buffer.put(method.toByteArray());
		}
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public static CompressionMethods from(final InputStream is) throws IOException {
		UInt8 size = UInt8.from(is);
		List<CompressionMethod> methods = new ArrayList<>();
		for (int i = 0; i < size.getValue(); ++i) {
			UInt8 int8 = UInt8.from(is);
			CompressionMethod method = CompressionMethod.fromValue(int8.getValue());
			methods.add(method);
		}

		return new CompressionMethods(size, methods);
	}

	public UInt8 getSize() {
		return size;
	}

	public List<CompressionMethod> getMethods() {
		return methods;
	}

	@Override
	public int sizeOf() {
		return size.sizeOf() + ByteSizeable.sizeOf(methods);
	}
}
