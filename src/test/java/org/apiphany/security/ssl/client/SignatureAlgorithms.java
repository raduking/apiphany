package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class SignatureAlgorithms implements TLSExtension {

	public static final Short[] ALGORITHMS = {
			(short) 0x0401, // RSA/PKCS1/SHA256
			(short) 0x0403, // ECDSA/SECP256r1/SHA256
			(short) 0x0501, // RSA/PKCS1/SHA384
			(short) 0x0503, // ECDSA/SECP384r1/SHA384
			(short) 0x0601, // RSA/PKCS1/SHA512
			(short) 0x0603, // ECDSA/SECP521r1/SHA512
			(short) 0x0201, // RSA/PKCS1/SHA1
			(short) 0x0203 // ECDSA/SHA1
	};

	private static final int BYTES_PER_ALGORITHM = 2;

	private ExtensionType type;

	private Int16 length;

	private Int16 listSize;

	private List<Short> algorithms;

	public SignatureAlgorithms(final ExtensionType type, final Int16 length, final Int16 listSize, final List<Short> algorithms) {
		this.type = type;
		this.length = length;
		this.listSize = listSize;
		this.algorithms = algorithms;
	}

	public SignatureAlgorithms(final Short... algorithms) {
		this(
				ExtensionType.SIGNATURE_ALGORITHMS,
				new Int16((short) (algorithms.length * BYTES_PER_ALGORITHM + Int16.BYTES)),
				new Int16((short) (algorithms.length * BYTES_PER_ALGORITHM)),
				List.of(algorithms)
		);
	}

	public SignatureAlgorithms() {
		this(ALGORITHMS);
	}

	public static SignatureAlgorithms from(final InputStream is) throws IOException {
		Int16 int16 = Int16.from(is);
		ExtensionType extensionType = ExtensionType.fromValue(int16.getValue());

		return from(is, extensionType);
	}

	public static SignatureAlgorithms from(final InputStream is, final ExtensionType type) throws IOException {
		Int16 length = Int16.from(is);
		Int16 listSize = Int16.from(is);
		List<Short> algorithms = new ArrayList<>();
		for (int i = 0; i < listSize.getValue() / BYTES_PER_ALGORITHM; ++i) {
			Int16 format = Int16.from(is);
			algorithms.add(format.getValue());
		}

		return new SignatureAlgorithms(type, length, listSize, algorithms);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.writeShort(type.value());
			dos.write(length.toByteArray());
			dos.write(listSize.toByteArray());
			for (Short algorithm : algorithms) {
				dos.writeShort(algorithm);
			}
		}).run();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return type.size() + length.size() + listSize.size() + algorithms.size() * BYTES_PER_ALGORITHM;
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

	public List<Short> getAlgorithms() {
		return algorithms;
	}
}
