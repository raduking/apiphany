package org.apiphany.security.ssl.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;
import org.morphix.lang.function.ThrowingRunnable;

public class Certificates implements TLSHandshakeBody {

	private Int24 length;

	private List<Certificate> list;

	public Certificates(final Int24 length, List<Certificate> list, boolean updateLength) {
		this.length = length;
		this.list = list;
		if (updateLength) {
			int result = length.size();
			for (Certificate certificate : list) {
				result += certificate.size();
			}
			this.length.setValue(result);
		}
	}

	public Certificates(final Int24 length, List<Certificate> list) {
		this(length, list, true);
	}

	public static Certificates from(final InputStream is) throws IOException {
		Int24 length = Int24.from(is);

		List<Certificate> certificates = new ArrayList<>();
		int certificatesLength = length.getValue();
		while (certificatesLength > 0) {
			Certificate certificate = Certificate.from(is);
			certificates.add(certificate);
			certificatesLength -= certificate.size();
		}

		return new Certificates(length, certificates, false);
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		ThrowingRunnable.unchecked(() -> {
			dos.write(length.toByteArray());
			for (Certificate certificate : list) {
				dos.write(certificate.toByteArray());
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
		int result = length.size();
		for (Certificate certificate : list) {
			result += certificate.size();
		}
		return result;
	}

	@Override
	public HandshakeType type() {
		return HandshakeType.CERTIFICATE;
	}

	public Int24 getLength() {
		return length;
	}

	public List<Certificate> getList() {
		return list;
	}
}
