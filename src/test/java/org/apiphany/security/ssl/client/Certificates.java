package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.json.JsonBuilder;

public class Certificates implements TLSHandshakeBody {

	private final Int24 length;

	private final List<Certificate> list;

	public Certificates(final Int24 length, final List<Certificate> list, final boolean updateLength) {
		this.length = length;
		this.list = list;
		if (updateLength) {
			int result = length.sizeOf();
			for (Certificate certificate : list) {
				result += certificate.sizeOf();
			}
			this.length.setValue(result);
		}
	}

	public Certificates(final Int24 length, final List<Certificate> list) {
		this(length, list, true);
	}

	public static Certificates from(final InputStream is) throws IOException {
		Int24 length = Int24.from(is);

		List<Certificate> certificates = new ArrayList<>();
		int certificatesLength = length.getValue();
		while (certificatesLength > 0) {
			Certificate certificate = Certificate.from(is);
			certificates.add(certificate);
			certificatesLength -= certificate.sizeOf();
		}

		return new Certificates(length, certificates, false);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(sizeOf());
		buffer.put(length.toByteArray());
		for (Certificate certificate : list) {
			buffer.put(certificate.toByteArray());
		}
		return buffer.array();
	}

	@Override
	public String toString() {
		return JsonBuilder.toJson(this);
	}

	@Override
	public int sizeOf() {
		int result = length.sizeOf();
		for (Certificate certificate : list) {
			result += certificate.sizeOf();
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
