package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class SignatureAlgorithms implements TLSExtension {

	private ExtensionType type;

	private Int16 length;

	private Int16 listSize;

	private List<SignatureAlgorithm> algorithms;

	public SignatureAlgorithms(final ExtensionType type, final Int16 length, final Int16 listSize, final List<SignatureAlgorithm> algorithms) {
		this.type = type;
		this.length = length;
		this.listSize = listSize;
		this.algorithms = algorithms;
	}

	public SignatureAlgorithms(final List<SignatureAlgorithm> algorithms) {
		this(
				ExtensionType.SIGNATURE_ALGORITHMS,
				new Int16((short) (algorithms.size() * SignatureAlgorithm.BYTES + Int16.BYTES)),
				new Int16((short) (algorithms.size() * SignatureAlgorithm.BYTES)),
				algorithms
		);
	}

	public SignatureAlgorithms(final SignatureAlgorithm... algorithms) {
		this(List.of(algorithms));
	}

	public SignatureAlgorithms() {
		this(SignatureAlgorithm.STRONG_ALGORITHMS);
	}

	public static SignatureAlgorithms from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static SignatureAlgorithms from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);
		Int16 listSize = Int16.from(is);
		List<SignatureAlgorithm> algorithms = new ArrayList<>();
		for (int i = 0; i < listSize.getValue() / SignatureAlgorithm.BYTES; ++i) {
			Int16 int16 = Int16.from(is);
			SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.fromValue(int16.getValue());
			algorithms.add(signatureAlgorithm);
		}

		return new SignatureAlgorithms(type, length, listSize, algorithms);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(listSize.toByteArray());
		for (SignatureAlgorithm algorithm : algorithms) {
			buffer.put(algorithm.toByteArray());
		}
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		return type.sizeOf() + length.sizeOf() + listSize.sizeOf() + algorithms.size() * SignatureAlgorithm.BYTES;
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public Int16 getLength() {
		return length;
	}

	public Int16 getListSize() {
		return listSize;
	}

	public List<SignatureAlgorithm> getAlgorithms() {
		return algorithms;
	}
}
