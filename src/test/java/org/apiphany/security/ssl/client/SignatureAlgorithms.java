package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class SignatureAlgorithms implements Sizeable {

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

	private ExtensionType type = ExtensionType.SIGNATURE_ALGORITHMS;

	private Int16 size;

	private Int16 listSize;

	private List<Short> algorithms;

	public SignatureAlgorithms(final Short... algorithms) {
		this.listSize = new Int16((short) (algorithms.length * BYTES_PER_ALGORITHM));
		this.size = new Int16((short) (listSize.getValue() + listSize.size()));
		this.algorithms = List.of(algorithms);
	}

	public SignatureAlgorithms() {
		this(ALGORITHMS);
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		dos.writeShort(type.value());
		dos.write(size.toByteArray());
		dos.write(listSize.toByteArray());
		for (Short algorithm : algorithms) {
			dos.writeShort(algorithm);
		}

		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int size() {
		return type.size() + size.size() + listSize.size() + algorithms.size() * BYTES_PER_ALGORITHM;
	}

	public ExtensionType getType() {
		return type;
	}

	public Int16 getSize() {
		return size;
	}

	public Int16 getListSize() {
		return listSize;
	}

	public List<Short> getAlgorithms() {
		return algorithms;
	}
}
