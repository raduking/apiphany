package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt16;
import org.apiphany.json.JsonBuilder;

public class SignatureAlgorithms implements TLSExtension {

	private final ExtensionType type;

	private final UInt16 length;

	private final UInt16 algorithmsSize;

	private final List<SignatureAlgorithm> algorithms;

	public SignatureAlgorithms(final ExtensionType type, final UInt16 length, final UInt16 algorithmsSize, final List<SignatureAlgorithm> algorithms) {
		this.type = type;
		this.length = length;
		this.algorithmsSize = algorithmsSize;
		this.algorithms = algorithms;
	}

	public SignatureAlgorithms(final List<SignatureAlgorithm> algorithms) {
		this(
				ExtensionType.SIGNATURE_ALGORITHMS,
				UInt16.of((short) (algorithms.size() * SignatureAlgorithm.BYTES + UInt16.BYTES)),
				UInt16.of((short) (algorithms.size() * SignatureAlgorithm.BYTES)),
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
		UInt16 int16 = UInt16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static SignatureAlgorithms from(final InputStream is, final ExtensionType type) throws IOException {
		UInt16 length = UInt16.from(is);
		UInt16 algorithmsSize = UInt16.from(is);
		List<SignatureAlgorithm> algorithms = new ArrayList<>();
		for (int i = 0; i < algorithmsSize.getValue() / SignatureAlgorithm.BYTES; ++i) {
			UInt16 int16 = UInt16.from(is);
			SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.fromValue(int16.getValue());
			algorithms.add(signatureAlgorithm);
		}

		return new SignatureAlgorithms(type, length, algorithmsSize, algorithms);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(type.toByteArray());
		buffer.put(length.toByteArray());
		buffer.put(algorithmsSize.toByteArray());
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
		return type.sizeOf() + length.sizeOf() + algorithmsSize.sizeOf() + algorithms.size() * SignatureAlgorithm.BYTES;
	}

	@Override
	public ExtensionType getType() {
		return type;
	}

	public UInt16 getLength() {
		return length;
	}

	public UInt16 getAlgorithmsSize() {
		return algorithmsSize;
	}

	public List<SignatureAlgorithm> getAlgorithms() {
		return algorithms;
	}
}
