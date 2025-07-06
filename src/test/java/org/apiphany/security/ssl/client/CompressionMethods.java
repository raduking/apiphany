package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class CompressionMethods implements Sizeable {

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
		this(List.of(new CompressionMethod(CompressionMethodType.NO_COMPRESSION)));
	}

	public byte[] toByteArray() {
		byte[] result = new byte[size.getValue() + 1];
		result[0] = size.getValue();
		for (int i = 0; i < size.getValue(); ++i) {
			result[i + 1] = methods.get(i).getMethod().value();
		}
		return result;
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	public static CompressionMethods from(final InputStream is) throws IOException {
		Int8 size = Int8.from(is);
		List<CompressionMethod> methods = new ArrayList<>();
		for (int i = 0; i < size.getValue(); ++i) {
			CompressionMethod method = CompressionMethod.from(is);
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
	public int size() {
		int result = size.size();
		for (CompressionMethod method : methods) {
			result += method.size();
		}
		return result;
	}
}
