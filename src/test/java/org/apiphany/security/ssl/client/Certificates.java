package org.apiphany.security.ssl.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apiphany.io.UInt24;
import org.apiphany.json.JsonBuilder;
import org.apiphany.lang.ByteSizeable;

public class Certificates implements TLSHandshakeBody {

	private final UInt24 length;

	private final List<Certificate> list;

	public Certificates(final UInt24 length, final List<Certificate> list, final boolean updateLength) {
		this.list = list;
		this.length = updateLength ? UInt24.of(length.sizeOf() + ByteSizeable.sizeOf(list)) : length;
	}

	public Certificates(final UInt24 length, final List<Certificate> list) {
		this(length, list, true);
	}

	public static Certificates from(final InputStream is) throws IOException {
		UInt24 length = UInt24.from(is);

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
		return length.sizeOf() + ByteSizeable.sizeOf(list);
	}

	@Override
	public HandshakeType getType() {
		return HandshakeType.CERTIFICATE;
	}

	public UInt24 getLength() {
		return length;
	}

	public List<Certificate> getList() {
		return list;
	}
}
