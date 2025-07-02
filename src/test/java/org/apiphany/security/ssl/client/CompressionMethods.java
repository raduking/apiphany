package org.apiphany.security.ssl.client;

import java.util.ArrayList;
import java.util.List;

public class CompressionMethods {

	private Int8 size;

	private List<CompressionMethod> methods;

	public CompressionMethods(List<CompressionMethod> methods) {
		this.size = new Int8((byte) methods.size());
		this.methods = new ArrayList<>(this.size.getValue());
		this.methods.addAll(methods);
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
}
