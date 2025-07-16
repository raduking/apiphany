package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class CompressionMethods implements TLSObject {

	private Int8 size;

	private List<CompressionMethod> methods;

	public CompressionMethods(final Int8 size, final List<CompressionMethod> methods) {
		this.size = size;
		this.methods = new ArrayList<>(this.size.getValue());
		this.methods.addAll(methods);
	}

	public CompressionMethods(final List<CompressionMethod> methods) {
		this(new Int8((byte) methods.size()), methods);
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
		Int8 size = Int8.from(is);
		List<CompressionMethod> methods = new ArrayList<>();
		for (int i = 0; i < size.getValue(); ++i) {
			Int8 int8 = Int8.from(is);
			CompressionMethod method = CompressionMethod.fromValue(int8.getValue());
			methods.add(method);
		}

		return new CompressionMethods(size, methods);
	}

	public Int8 getSize() {
		return size;
	}

	public List<CompressionMethod> getMethods() {
		return methods;
	}

	@Override
	public int sizeOf() {
		int result = size.sizeOf();
		for (CompressionMethod method : methods) {
			result += method.sizeOf();
		}
		return result;
	}
}
